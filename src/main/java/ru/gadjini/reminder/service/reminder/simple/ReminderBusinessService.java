package ru.gadjini.reminder.service.reminder.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class ReminderBusinessService {

    private ReminderDao reminderDao;

    private ReminderNotificationService reminderNotificationService;

    private ReminderService reminderService;

    @Autowired
    public ReminderBusinessService(ReminderDao reminderDao,
                                   ReminderNotificationService reminderNotificationService,
                                   ReminderService reminderService) {
        this.reminderDao = reminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderService = reminderService;
    }

    @Transactional
    public Reminder completeReminder(int id) {
        reminderService.stopWork(id);
        Reminder completed = reminderDao.update(
                Map.of(ReminderTable.TABLE.STATUS, Reminder.Status.COMPLETED.getCode()),
                ReminderTable.TABLE.STATUS.equal(Reminder.Status.ACTIVE.getCode()).and(ReminderTable.TABLE.ID.equal(id)),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
        reminderNotificationService.deleteReminderNotifications(id);

        return completed;
    }

    @Transactional
    public Reminder postponeReminder(int reminderId, long receiverId, DateTime remindAtInReceiverZone) {
        DateTime remindAt = remindAtInReceiverZone.withZoneSameInstant(ZoneOffset.UTC);
        Reminder reminder = reminderDao.update(
                Map.of(ReminderTable.TABLE.REMIND_AT, remindAt.sqlObject()),
                ReminderTable.TABLE.ID.equal(reminderId),
                new ReminderMapping().setCreatorMapping(new Mapping()).setReceiverMapping(new Mapping())
        );

        List<ReminderNotification> reminderNotifications = reminderService.getReminderNotifications(remindAt, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.deleteReminderNotifications(reminderId);
        reminderNotificationService.create(reminderNotifications);

        return reminder;
    }

    public Reminder cancel(int reminderId) {
        List<Reminder> reminders = reminderDao.delete(
                ReminderTable.TABLE.ID.equal(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );

        return reminders.isEmpty() ? null : reminders.iterator().next();
    }
}
