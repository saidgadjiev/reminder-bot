package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationAI;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private ReminderNotificationService reminderNotificationService;

    private UserReminderNotificationService userReminderNotificationService;

    private ReminderNotificationAI reminderNotificationAI;

    @Autowired
    public ReminderService(ReminderDao reminderDao,
                           ReminderNotificationService reminderNotificationService,
                           UserReminderNotificationService userReminderNotificationService,
                           ReminderNotificationAI reminderNotificationAI) {
        this.reminderDao = reminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.userReminderNotificationService = userReminderNotificationService;
        this.reminderNotificationAI = reminderNotificationAI;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderNotification> reminderNotifications = getReminderNotifications(reminder.getRemindAt(), reminder.getReceiverId());
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(created.getId()));
        reminderNotificationService.create(reminderNotifications);

        return reminder;
    }

    public Reminder deactivate(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.STATUS, Reminder.Status.INACTIVE.getCode()),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setRemindMessageMapping(new Mapping())
        );
    }

    public Reminder read(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.READ, true),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setRemindMessageMapping(new Mapping())
        );
    }

    public Reminder activate(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.STATUS, Reminder.Status.ACTIVE.getCode()),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setRemindMessageMapping(new Mapping())
        );
    }

    public void updateReminderNotifications(int reminderId, int receiverId, DateTime remindAt) {
        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> reminderNotifications = getReminderNotifications(remindAt, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);
    }

    public Reminder getReminderByMessageId(int messageId, ReminderMapping reminderMapping) {
        return reminderDao.getReminder(ReminderTable.TABLE.as("r").MESSAGE_ID.eq(messageId), reminderMapping);
    }

    public void updateReminder(int id, Map<Field<?>, Object> updateValues) {
        if (updateValues.get(ReminderTable.TABLE.REMIND_AT) != null) {
            updateValues.put(ReminderTable.TABLE.INITIAL_REMIND_AT, updateValues.get(ReminderTable.TABLE.REMIND_AT));
        }
        reminderDao.update(updateValues, ReminderTable.TABLE.ID.eq(id), null);
    }

    @Transactional
    public Reminder changeReminderTime(int reminderId, int receiverId, DateTime remindAt) {
        Map<Field<?>, Object> updateValues = new HashMap<>();
        updateValues.put(ReminderTable.TABLE.INITIAL_REMIND_AT, remindAt.sqlObject());
        updateValues.put(ReminderTable.TABLE.REMIND_AT, remindAt.sqlObject());
        updateValues.put(ReminderTable.TABLE.REPEAT_REMIND_AT, null);

        Reminder reminder = reminderDao.update(
                updateValues,
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping())
                        .setRemindMessageMapping(new Mapping())
        );

        List<ReminderNotification> reminderNotifications = getReminderNotifications(remindAt, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.deleteReminderNotifications(reminderId);
        reminderNotificationService.create(reminderNotifications);

        return reminder;
    }

    public Reminder changeReminderNote(int reminderId, String note) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.NOTE, note),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setRemindMessageMapping(new Mapping())
        );
    }

    public Reminder deleteReminderNote(int reminderId) {
        Map<Field<?>, Object> updateValues = new HashMap<>();
        updateValues.put(ReminderTable.TABLE.NOTE, null);

        return reminderDao.update(
                updateValues,
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setRemindMessageMapping(new Mapping())
        );
    }

    public UpdateReminderResult changeReminderText(int reminderId, String newText) {
        return reminderDao.updateReminderText(reminderId, StringUtils.capitalize(newText.toLowerCase()));
    }

    public List<Reminder> getCompletedReminders(int userId) {
        return reminderDao.getCompletedReminders(userId);
    }

    public Reminder getReminder(int reminderId, ReminderMapping reminderMapping) {
        return reminderDao.getReminder(ReminderTable.TABLE.as("r").ID.eq(reminderId), reminderMapping);
    }

    public Reminder getReminder(int reminderId) {
        return reminderDao.getReminder(
                ReminderTable.TABLE.as("r").ID.eq(reminderId),
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        return reminderDao.getRemindersWithReminderTimes(localDateTime, limit);
    }

    @Transactional
    public Reminder completeReminder(int id) {
        Reminder completed = reminderDao.update(
                Map.of(ReminderTable.TABLE.STATUS, Reminder.Status.COMPLETED.getCode()),
                ReminderTable.TABLE.STATUS.equal(Reminder.Status.ACTIVE.getCode()).and(ReminderTable.TABLE.ID.equal(id)),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setRemindMessageMapping(new Mapping())
        );
        reminderNotificationService.deleteReminderNotifications(id);

        return completed;
    }

    public void deleteMyCompletedReminders(int userId) {
        reminderDao.deleteCompletedReminders(userId);
    }

    public int deleteCompletedReminders(LocalDateTime localDateTime) {
        return reminderDao.deleteCompletedReminders(localDateTime);
    }

    public List<Reminder> getActiveReminders(int userId) {
        return reminderDao.getActiveReminders(userId);
    }

    public Reminder delete(int reminderId) {
        List<Reminder> reminders = reminderDao.delete(
                ReminderTable.TABLE.ID.equal(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping())
                        .setRemindMessageMapping(new Mapping())
        );

        return reminders.isEmpty() ? null : reminders.iterator().next();
    }

    public List<Reminder> deleteFriendReminders(int userId, int friendId) {
        return reminderDao.delete(
                ReminderTable.TABLE.CREATOR_ID.eq(userId).and(ReminderTable.TABLE.RECEIVER_ID.eq(friendId))
                        .or(ReminderTable.TABLE.CREATOR_ID.eq(friendId).and(ReminderTable.TABLE.RECEIVER_ID.eq(userId))),
                new ReminderMapping()
                        .setRemindMessageMapping(new Mapping())
        );
    }

    @Transactional
    public Reminder postponeReminder(int reminderId, int receiverId, DateTime remindAtInReceiverZone) {
        DateTime remindAt = remindAtInReceiverZone.withZoneSameInstant(ZoneOffset.UTC);
        Reminder reminder = reminderDao.update(
                Map.of(ReminderTable.TABLE.REMIND_AT, remindAt.sqlObject()),
                ReminderTable.TABLE.ID.equal(reminderId),
                new ReminderMapping().setCreatorMapping(new Mapping()).setReceiverMapping(new Mapping()).setRemindMessageMapping(new Mapping())
        );

        List<ReminderNotification> reminderNotifications = getReminderNotifications(remindAt, receiverId);
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
                        .setRemindMessageMapping(new Mapping())
        );

        return reminders.isEmpty() ? null : reminders.iterator().next();
    }

    public ReminderNotification customRemind(int reminderId, ZonedDateTime remindTime) {
        ReminderNotification reminderNotification = new ReminderNotification();
        reminderNotification.setType(ReminderNotification.Type.ONCE);
        reminderNotification.setReminderId(reminderId);
        reminderNotification.setFixedTime(remindTime);
        reminderNotification.setCustom(true);
        reminderNotificationService.create(reminderNotification);

        return reminderNotification;
    }

    public ReminderNotification customRemind(int reminderId, RepeatTime repeatTime) {
        ReminderNotification reminderNotification;
        ZonedDateTime now = TimeUtils.nowZoned();

        if (repeatTime.getDayOfWeek() != null) {
            ZonedDateTime repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());
            reminderNotification = fixedRepeatReminderTime(repeatReminder.toLocalDate(), 7, repeatTime.getTime());
        } else if (repeatTime.getInterval().getDays() > 0) {
            ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

            if (repeatReminder.isBefore(now)) {
                repeatReminder = repeatReminder.plusDays(repeatTime.getInterval().getDays());
            }
            reminderNotification = fixedRepeatReminderTime(repeatReminder.toLocalDate(), repeatTime.getInterval().getDays(), repeatTime.getTime());
        } else {
            reminderNotification = intervalReminderTime(now, repeatTime.getInterval());
        }
        reminderNotification.setCustom(true);
        reminderNotification.setReminderId(reminderId);
        reminderNotificationService.create(reminderNotification);

        return reminderNotification;
    }

    private List<ReminderNotification> getReminderNotifications(DateTime dateTime, int receiverId) {
        if (!dateTime.hasTime()) {
            return getReminderNotificationsWithoutTime(dateTime.date(), receiverId);
        }

        return getReminderNotifications(dateTime.toZonedDateTime(), receiverId);
    }

    private List<ReminderNotification> getReminderNotificationsWithoutTime(LocalDate localDate, int receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (reminderNotificationAI.isNeedCreateReminderNotification(localDate, userReminderNotification)) {
                fixedReminderTime(localDate.minusDays(userReminderNotification.getDays()), userReminderNotification.getTime(), reminderNotifications).setCustom(true);
            }
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getReminderNotifications(ZonedDateTime remindAt, int receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);
        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (reminderNotificationAI.isNeedCreateReminderNotification(remindAt, userReminderNotification)) {
                fixedReminderTime(
                        remindAt.toLocalDate().minusDays(userReminderNotification.getDays()),
                        userReminderNotification.getTime() == null ? remindAt.toLocalTime().minusHours(userReminderNotification.getHours()).minusMinutes(userReminderNotification.getMinutes()) : userReminderNotification.getTime(),
                        reminderNotifications
                ).setCustom(true);
            }
        }

        ReminderNotification intervalNotification = intervalReminderTime(remindAt, new Period().withMinutes(20));
        intervalNotification.setCustom(true);
        reminderNotifications.add(intervalNotification);
        if (reminderNotificationAI.isNeedCreateItsTimeNotification(remindAt)) {
            fixedReminderTime(remindAt.toLocalDate(), remindAt.toLocalTime(), reminderNotifications).setItsTime(true);
        }

        return reminderNotifications;
    }

    private ReminderNotification fixedReminderTime(LocalDate date, LocalTime time, List<ReminderNotification> reminderNotifications) {
        ReminderNotification fixedTime = ReminderNotification.onceTime();
        fixedTime.setFixedTime(ZonedDateTime.of(date, time, ZoneOffset.UTC));
        reminderNotifications.add(fixedTime);

        return fixedTime;
    }

    private ReminderNotification fixedRepeatReminderTime(LocalDate repeatAt, int repeatDays, LocalTime localTime) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(ZonedDateTime.of(repeatAt.minusDays(repeatDays), localTime, ZoneOffset.UTC));
        reminderNotification.setDelayTime(new Period().withDays(repeatDays));

        return reminderNotification;
    }

    private ReminderNotification intervalReminderTime(ZonedDateTime remindAt, Period interval) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(remindAt);
        reminderNotification.setDelayTime(interval);

        return reminderNotification;
    }
}
