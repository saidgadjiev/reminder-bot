package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.security.SecurityService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepeatReminderService {

    private ReminderDao reminderDao;

    private SecurityService securityService;

    private ReminderTimeService reminderTimeService;

    private TgUserService userService;

    @Autowired
    public RepeatReminderService(ReminderDao reminderDao, SecurityService securityService, ReminderTimeService reminderTimeService) {
        this.reminderDao = reminderDao;
        this.securityService = securityService;
        this.reminderTimeService = reminderTimeService;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderTime> reminderTimes = getRepeatReminderTimes(reminder.getRepeatRemindAt());
        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(created.getId()));
        reminderTimeService.create(reminderTimes);

        return created;
    }

    private List<ReminderTime> getRepeatReminderTimes(RepeatTime repeatTime) {
        User user = securityService.getAuthenticatedUser();
        ZoneId zoneId = userService.getTimeZone(user.getId());

        List<ReminderTime> reminderTimes = new ArrayList<>();
        if (repeatTime.getDayOfWeek() != null) {
            addWeeklyReminderTimes(repeatTime, zoneId, reminderTimes);
        } else if (repeatTime.getInterval().getDays() > 0) {
            addDailyReminderTimes(repeatTime, zoneId, reminderTimes);
        } else {
            addIntervalReminderTimes(repeatTime, zoneId, reminderTimes);
        }

        return reminderTimes;
    }

    private void addIntervalReminderTimes(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        intervalReminderTime(now, repeatTime.getInterval(), 0, reminderTimes);

        int minutes = repeatTime.getInterval().toStandardMinutes().getMinutes();
        if (minutes > 20) {
            intervalReminderTime(now, repeatTime.getInterval(), 20, reminderTimes);
        }
        if (minutes > 60) {
            intervalReminderTime(now, repeatTime.getInterval(), 60, reminderTimes);
        }
        if (minutes > 120) {
            intervalReminderTime(now, repeatTime.getInterval(), 120, reminderTimes);
        }
    }

    private void intervalReminderTime(ZonedDateTime remindAt, Period interval, int minutes, List<ReminderTime> reminderTimes) {
        ReminderTime reminderTime = new ReminderTime();
        reminderTime.setType(ReminderTime.Type.REPEAT);
        reminderTime.setLastReminderAt(remindAt.minusMinutes(minutes));
        reminderTime.setDelayTime(interval);
        reminderTimes.add(reminderTime);
    }

    private void addDailyReminderTimes(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

        beforeHoursReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 0, reminderTimes);
        beforeMinutesReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 20, reminderTimes);
        beforeHoursReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 1, reminderTimes);
        beforeHoursReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 2, reminderTimes);
        nightBeforeReminderTime(repeatReminder, repeatTime.getInterval().getDays(), reminderTimes);
    }

    private void addWeeklyReminderTimes(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());

        beforeHoursReminderTime(repeatReminder, 7, 0, reminderTimes);
        beforeMinutesReminderTime(repeatReminder, 7, 20, reminderTimes);
        beforeHoursReminderTime(repeatReminder, 7, 1, reminderTimes);
        beforeHoursReminderTime(repeatReminder, 7, 2, reminderTimes);
        nightBeforeReminderTime(repeatReminder, 7, reminderTimes);
    }

    private void nightBeforeReminderTime(ZonedDateTime repeatAt, int days, List<ReminderTime> reminderTimes) {
        ReminderTime reminderTimeNightBefore = new ReminderTime();
        reminderTimeNightBefore.setType(ReminderTime.Type.REPEAT);
        reminderTimeNightBefore.setLastReminderAt(repeatAt.minusDays(days + 1).with(LocalTime.of(22, 0)));
        reminderTimeNightBefore.setDelayTime(new Period().withDays(days));
        reminderTimes.add(reminderTimeNightBefore);
    }

    private void beforeMinutesReminderTime(ZonedDateTime remindAt, int days, int minutes, List<ReminderTime> reminderTimes) {
        ReminderTime reminderTimeMinutes = new ReminderTime();
        reminderTimeMinutes.setType(ReminderTime.Type.REPEAT);
        reminderTimeMinutes.setLastReminderAt(remindAt.minusDays(days).minusMinutes(minutes));
        reminderTimeMinutes.setDelayTime(new Period().withDays(days));
        reminderTimes.add(reminderTimeMinutes);
    }

    private void beforeHoursReminderTime(ZonedDateTime remindAt, int days, int hours, List<ReminderTime> reminderTimes) {
        ReminderTime beforeHourReminderTime = new ReminderTime();
        beforeHourReminderTime.setType(ReminderTime.Type.REPEAT);
        beforeHourReminderTime.setLastReminderAt(remindAt.minusDays(days).minusHours(hours));
        beforeHourReminderTime.setDelayTime(new Period().withDays(days));
        reminderTimes.add(beforeHourReminderTime);
    }

    public static void main(String[] args) {
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime repeatTime = now.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).with(LocalTime.of(19, 0));

    }
}
