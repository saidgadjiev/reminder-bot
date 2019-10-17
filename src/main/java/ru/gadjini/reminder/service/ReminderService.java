package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.util.DateUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private TgUserService tgUserService;

    private ReminderTimeService reminderTimeService;

    @Autowired
    public ReminderService(ReminderDao reminderDao, TgUserService tgUserService, ReminderTimeService reminderTimeService) {
        this.reminderDao = reminderDao;
        this.tgUserService = tgUserService;
        this.reminderTimeService = reminderTimeService;
    }

    @Transactional
    public void createReminder(ReminderRequest reminderRequest) {
        Reminder reminder = new Reminder();

        reminder.setRemindAt(reminderRequest.getRemindAt());
        reminder.setText(reminderRequest.getText());

        int creatorId = tgUserService.getUserId(reminderRequest.getCreatorName());
        reminder.setCreatorId(creatorId);

        int receiverId = tgUserService.getUserId(reminderRequest.getReceiverName());
        reminder.setReceiverId(receiverId);

        reminder = reminderDao.create(reminder);

        if (reminder.getRemindAt().minusHours(1).isAfter(DateUtils.now())) {
            ReminderTime oneHourFixedTime = new ReminderTime();
            oneHourFixedTime.setType(ReminderTime.Type.ONCE);
            oneHourFixedTime.setReminderId(reminder.getId());
            oneHourFixedTime.setFixedTime(reminder.getRemindAt().minusHours(1));
            reminderTimeService.create(oneHourFixedTime);
        }

        ReminderTime itsTimeFixedTime = new ReminderTime();
        itsTimeFixedTime.setType(ReminderTime.Type.ONCE);
        itsTimeFixedTime.setFixedTime(reminder.getRemindAt());
        itsTimeFixedTime.setReminderId(reminder.getId());
        reminderTimeService.create(itsTimeFixedTime);

        ReminderTime fiveMinuteDelayTime = new ReminderTime();
        fiveMinuteDelayTime.setType(ReminderTime.Type.REPEAT);
        fiveMinuteDelayTime.setReminderId(reminder.getId());
        fiveMinuteDelayTime.setDelayTime(LocalTime.of(0, 5));
        if (reminder.getRemindAt().minusMinutes(5).isBefore(DateUtils.now())) {
            fiveMinuteDelayTime.setLastReminderAt(DateUtils.now());
        }
        reminderTimeService.create(fiveMinuteDelayTime);
    }

    public List<Reminder> getReminders(LocalDateTime localDateTime) {
        return reminderDao.getReminders(localDateTime);
    }

    public Reminder deleteReminder(int id) {
        Reminder reminder = reminderDao.delete(id);

        Map<Integer, TgUser> tgUsers = tgUserService.getUsersByIds(Stream.of(reminder.getCreatorId(), reminder.getReceiverId()).collect(Collectors.toSet()));

        reminder.setReceiver(tgUsers.get(reminder.getReceiverId()));
        reminder.setCreator(tgUsers.get(reminder.getCreatorId()));

        return reminder;
    }
}
