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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private TgUserService tgUserService;

    private ReminderTimeService reminderTimeService;

    private SecurityService securityService;

    @Autowired
    public ReminderService(ReminderDao reminderDao, TgUserService tgUserService, ReminderTimeService reminderTimeService, SecurityService securityService) {
        this.reminderDao = reminderDao;
        this.tgUserService = tgUserService;
        this.reminderTimeService = reminderTimeService;
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
        receiver.setUsername(reminderRequest.getReceiverName());
        reminder.setReceiver(receiver);
        reminder = reminderDao.create(reminder);

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

        reminderTimeService.create(reminderTimes);

        return reminder;
    }

    public List<Reminder> getReminders(LocalDateTime localDateTime) {
        return reminderDao.getReminders(localDateTime);
    }

    public Reminder deleteReminder(int id) {
        Reminder reminder = reminderDao.delete(id);

        Map<Integer, TgUser> tgUsers = tgUserService.getUsersByUserIds(Stream.of(reminder.getCreatorId(), reminder.getReceiverId()).collect(Collectors.toSet()));

        reminder.setReceiver(tgUsers.get(reminder.getReceiverId()));
        reminder.setCreator(tgUsers.get(reminder.getCreatorId()));

        return reminder;
    }
}
