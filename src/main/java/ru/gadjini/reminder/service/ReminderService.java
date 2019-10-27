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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    private ReminderTimeService reminderTimeService;

    @Autowired
    public ReminderService(ReminderDao reminderDao, SecurityService securityService, ReminderTimeService reminderTimeService) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
        this.reminderTimeService = reminderTimeService;
    }

    @Transactional
    public Reminder createReminder(ReminderRequest reminderRequest) {
        Reminder reminder = new Reminder();

        reminder.setRemindAt(DateUtils.toUtc(reminderRequest.getRemindAt()));
        reminder.setText(reminderRequest.getText());

        User user = securityService.getAuthenticatedUser();
        TgUser creator = new TgUser();
        creator.setId(user.getId());
        creator.setFirstName(user.getFirstName());
        creator.setLastName(user.getLastName());
        reminder.setCreator(creator);
        reminder.setCreatorId(user.getId());

        if (reminderRequest.isForMe()) {
            prepareReminderForMe(reminder);
        } else {
            prepareReminderForAnother(reminder, reminderRequest);
        }

        Reminder created = reminderDao.create(reminder);

        List<ReminderTime> reminderTimes = getReminderTimes(reminderRequest.getRemindAt());

        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(created.getId()));

        reminderTimeService.create(reminderTimes);

        return reminder;
    }

    @Transactional
    public Reminder changeReminderTime(int reminderId, ZonedDateTime remindAtInUserTimeZone) {
        ZonedDateTime remindAt = remindAtInUserTimeZone.withZoneSameInstant(ZoneOffset.UTC);

        Reminder reminder = reminderDao.updateRemindAt(reminderId, remindAt);

        reminderTimeService.deleteReminderTimes(reminderId);
        List<ReminderTime> reminderTimes = getReminderTimes(remindAt);

        reminderTimeService.deleteReminderTimes(reminderId);
        reminderTimeService.create(reminderTimes);

        return reminder;
    }

    public List<Reminder> getReminders() {
        User user = securityService.getAuthenticatedUser();

        return reminderDao.getReminders(user.getId());
    }

    public List<Reminder> getRemindersWithReminderTimes(LocalDateTime localDateTime, int limit) {
        return reminderDao.getRemindersWithReminderTimes(localDateTime, limit);
    }

    @Transactional
    public Reminder deleteReminder(int id) {
        return reminderDao.delete(id);
    }

    public Reminder getReminder(int reminderId) {
        return reminderDao.getReminder(reminderId);
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
        }

        reminder.setReceiver(receiver);
    }

    private List<ReminderTime> getReminderTimes(ZonedDateTime remindAt) {
        List<ReminderTime> reminderTimes = new ArrayList<>();

        addNightBeforeReminderTime(remindAt, reminderTimes);
        addFixedTime(remindAt, 1, reminderTimes);
        addDelayTime(remindAt, 10, reminderTimes);
        addItsTimeFixedTime(remindAt, reminderTimes);

        return reminderTimes;
    }

    private void addItsTimeFixedTime(ZonedDateTime remindAt, List<ReminderTime> reminderTimes) {
        ReminderTime itsTimeFixedTime = new ReminderTime();
        itsTimeFixedTime.setType(ReminderTime.Type.ONCE);
        itsTimeFixedTime.setFixedTime(DateUtils.toUtc(remindAt));
        reminderTimes.add(itsTimeFixedTime);
    }

    private void addFixedTime(ZonedDateTime remindAt, int hour, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (remindAt.minusHours(hour).isAfter(now)) {
            ReminderTime oneHourFixedTime = new ReminderTime();
            oneHourFixedTime.setType(ReminderTime.Type.ONCE);
            oneHourFixedTime.setFixedTime(DateUtils.toUtc(remindAt.minusHours(1)));
            reminderTimes.add(oneHourFixedTime);
        }
    }

    private void addDelayTime(ZonedDateTime remindAt, int delayMinute, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        ReminderTime delayTime = new ReminderTime();
        delayTime.setType(ReminderTime.Type.REPEAT);
        delayTime.setDelayTime(LocalTime.of(0, delayMinute));
        if (remindAt.minusMinutes(10).isBefore(now)) {
            delayTime.setLastReminderAt(DateUtils.toUtc(remindAt));
        }
        reminderTimes.add(delayTime);
    }

    private void addNightBeforeReminderTime(ZonedDateTime remindAt, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (remindAt.getDayOfMonth() > now.getDayOfMonth()) {
            if (remindAt.getDayOfMonth() - now.getDayOfMonth() > 1) {
                ReminderTime reminderTime = new ReminderTime();

                reminderTime.setType(ReminderTime.Type.ONCE);
                reminderTime.setFixedTime(DateUtils.toUtc(remindAt.minusDays(1).with(LocalTime.of(22, 0))));

                reminderTimes.add(reminderTime);
            } else if (now.getHour() < 22) {
                ReminderTime reminderTime = new ReminderTime();

                reminderTime.setType(ReminderTime.Type.ONCE);
                reminderTime.setFixedTime(DateUtils.toUtc(now.with(LocalTime.of(22, 0))));

                reminderTimes.add(reminderTime);
            }
        }
    }
}
