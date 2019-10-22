package ru.gadjini.reminder.service;

import org.apache.commons.lang3.StringUtils;
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
        reminder.setReminderTimes(getReminderTimes(reminder.getRemindAt()));

        switch (reminderRequest.getMatchType()) {
            case LOGIN_TEXT_TIME:
                prepareReminderForAnother(reminder, reminderRequest);
                break;
            case TEXT_TIME:
                prepareReminderForMe(reminder);
                break;
        }

        return reminderDao.create(reminder);
    }

    public List<Reminder> getReminders(LocalDateTime localDateTime) {
        return reminderDao.getReminders(localDateTime);
    }

    @Transactional
    public Reminder deleteReminder(int id) {
        return reminderDao.delete(id);
    }

    private void prepareReminderForMe(Reminder reminder) {
        TgUser receiver = new TgUser();

        receiver.setUserId(reminder.getCreatorId());
        reminder.setReceiver(receiver);
        reminder.setReceiverId(reminder.getCreatorId());
    }

    private void prepareReminderForAnother(Reminder reminder, ReminderRequest reminderRequest) {
        TgUser receiver = new TgUser();

        if (StringUtils.isNotBlank(reminderRequest.getReceiverName())) {
            receiver.setUsername(reminderRequest.getReceiverName());
        } else {
            receiver.setUserId(reminderRequest.getReceiverId());
            reminder.setReceiverId(reminderRequest.getReceiverId());
        }
    }

    private List<ReminderTime> getReminderTimes(LocalDateTime remindAt) {
        List<ReminderTime> reminderTimes = new ArrayList<>();
        if (remindAt.minusHours(1).isAfter(DateUtils.now())) {
            ReminderTime oneHourFixedTime = new ReminderTime();
            oneHourFixedTime.setType(ReminderTime.Type.ONCE);
            oneHourFixedTime.setFixedTime(remindAt.minusHours(1));
            reminderTimes.add(oneHourFixedTime);
        }

        ReminderTime itsTimeFixedTime = new ReminderTime();
        itsTimeFixedTime.setType(ReminderTime.Type.ONCE);
        itsTimeFixedTime.setFixedTime(remindAt);
        reminderTimes.add(itsTimeFixedTime);

        ReminderTime delayTime = new ReminderTime();
        delayTime.setType(ReminderTime.Type.REPEAT);
        delayTime.setDelayTime(LocalTime.of(0, 10));
        if (remindAt.minusMinutes(10).isBefore(DateUtils.now())) {
            delayTime.setLastReminderAt(remindAt);
        }
        reminderTimes.add(delayTime);

        return reminderTimes;
    }
}
