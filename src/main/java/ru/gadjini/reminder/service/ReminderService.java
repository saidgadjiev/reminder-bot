package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.model.ReminderRequest;

import java.time.LocalTime;

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

        ReminderTime oneHourFixedTime = new ReminderTime();
        oneHourFixedTime.setType(ReminderTime.Type.ONCE);
        oneHourFixedTime.setReminderId(reminder.getId());
        oneHourFixedTime.setFixedTime(reminder.getRemindAt().minusHours(1));
        reminderTimeService.create(oneHourFixedTime);

        ReminderTime fiveMinuteDelayTime = new ReminderTime();
        fiveMinuteDelayTime.setType(ReminderTime.Type.REPEAT);
        fiveMinuteDelayTime.setReminderId(reminder.getId());
        fiveMinuteDelayTime.setDelayTime(LocalTime.of(0, 5));
        reminderTimeService.create(fiveMinuteDelayTime);
    }
}
