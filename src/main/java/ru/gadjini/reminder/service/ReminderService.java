package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.util.DateUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    @Autowired
    public ReminderService(ReminderDao reminderDao, SecurityService securityService) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
    }

    @Transactional
    public Reminder createReminder(ReminderRequest reminderRequest) {
        Reminder reminder = new Reminder();

        reminder.setRemindAt(reminderRequest.getRemindAt());
        reminder.setText(reminderRequest.getText());

        User user = securityService.getAuthenticatedUser();
        reminder.setCreatorId(user.getId());

        TgUser receiver = new TgUser();
        receiver.setId(reminderRequest.getReceiverId());
        receiver.setUsername(reminderRequest.getReceiverName());
        reminder.setReceiver(receiver);

        List<ReminderTime> reminderTimes = new ArrayList<>();
        if (reminder.getRemindAt().minusHours(1).isAfter(DateUtils.now())) {
            ReminderTime oneHourFixedTime = new ReminderTime();
            oneHourFixedTime.setType(ReminderTime.Type.ONCE);
            oneHourFixedTime.setReminderId(reminder.getId());
            oneHourFixedTime.setFixedTime(reminder.getRemindAt().minusHours(1));
            reminderTimes.add(oneHourFixedTime);
        }

        ReminderTime itsTimeFixedTime = new ReminderTime();
        itsTimeFixedTime.setType(ReminderTime.Type.ONCE);
        itsTimeFixedTime.setFixedTime(reminder.getRemindAt());
        itsTimeFixedTime.setReminderId(reminder.getId());
        reminderTimes.add(itsTimeFixedTime);

        ReminderTime fiveMinuteDelayTime = new ReminderTime();
        fiveMinuteDelayTime.setType(ReminderTime.Type.REPEAT);
        fiveMinuteDelayTime.setReminderId(reminder.getId());
        fiveMinuteDelayTime.setDelayTime(LocalTime.of(0, 5));
        if (reminder.getRemindAt().minusMinutes(5).isBefore(DateUtils.now())) {
            fiveMinuteDelayTime.setLastReminderAt(DateUtils.now());
        }
        reminderTimes.add(fiveMinuteDelayTime);

        reminder.setReminderTimes(reminderTimes);

        reminder = reminderDao.create(reminder);

        return reminder;
    }

    public List<Reminder> getReminders(LocalDateTime localDateTime) {
        return reminderDao.getReminders(localDateTime);
    }

    public Reminder deleteReminder(int id) {
        return reminderDao.delete(id);
    }
}
