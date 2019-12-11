package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeAI;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RepeatReminderService {

    private ReminderDao reminderDao;

    private ReminderTimeService reminderTimeService;

    private TgUserService userService;

    private ReminderTimeAI reminderTimeAI;

    @Autowired
    public RepeatReminderService(ReminderDao reminderDao, ReminderTimeService reminderTimeService, TgUserService userService, ReminderTimeAI reminderTimeAI) {
        this.reminderDao = reminderDao;
        this.reminderTimeService = reminderTimeService;
        this.userService = userService;
        this.reminderTimeAI = reminderTimeAI;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        ZoneId zoneId = userService.getTimeZone(reminder.getReceiverId());
        ZonedDateTime nextRemindAtInReceiverZone = getFirstRemindAt(zoneId, reminder.getRepeatRemindAt());
        reminder.setRemindAt(new DateTime(nextRemindAtInReceiverZone.withZoneSameInstant(ZoneOffset.UTC)));

        Reminder created = reminderDao.create(reminder);
        List<ReminderTime> reminderTimes = getRepeatReminderTimes(reminder.getRepeatRemindAt(), zoneId);
        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(created.getId()));
        reminderTimeService.create(reminderTimes);

        return created;
    }

    public void updateNextRemindAt(int reminderId, ZonedDateTime nextRemindAt) {
        LocalDateTime localDateTime = nextRemindAt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.REMIND_AT, timestamp);
                    put(ReminderTable.TABLE.INITIAL_REMIND_AT, timestamp);
                }},
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
    }

    public ZonedDateTime getNextRemindAt(ZonedDateTime remindAt, RepeatTime repeatTime) {
        if (repeatTime.getDayOfWeek() != null) {
            return getWeeklyNextRemindAt(remindAt.getZone(), repeatTime);
        } else if (repeatTime.getInterval().getDays() > 0) {
            return getDailyNextRemindAt(remindAt, repeatTime);
        } else {
            return getIntervalNextRemindAt(remindAt, repeatTime);
        }
    }

    private ZonedDateTime getFirstRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        if (repeatTime.getDayOfWeek() != null) {
            return getWeeklyNextRemindAt(zoneId, repeatTime);
        } else if (repeatTime.getInterval().getDays() > 0) {
            return getDailyFirstRemindAt(zoneId, repeatTime);
        } else {
            return getIntervalFirstRemindAt(zoneId, repeatTime);
        }
    }

    private ZonedDateTime getIntervalNextRemindAt(ZonedDateTime lastRemindAt, RepeatTime repeatTime) {
        ZonedDateTime now = ZonedDateTime.now(lastRemindAt.getZone());

        while (now.isAfter(lastRemindAt)) {
            lastRemindAt = JodaTimeUtils.plus(lastRemindAt, repeatTime.getInterval());
        }

        return lastRemindAt;
    }

    private ZonedDateTime getIntervalFirstRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.now(zoneId);

        return now.plusHours(repeatTime.getInterval().getHours()).plusMinutes(repeatTime.getInterval().getMinutes());
    }

    private ZonedDateTime getWeeklyNextRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.now(zoneId);

        return now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());
    }

    private ZonedDateTime getDailyNextRemindAt(ZonedDateTime lastRemindAt, RepeatTime repeatTime) {
        return lastRemindAt.plusDays(repeatTime.getInterval().getDays());
    }

    private ZonedDateTime getDailyFirstRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.now(zoneId);
        ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

        if (repeatReminder.isBefore(now)) {
            repeatReminder.plusDays(repeatTime.getInterval().getDays());
        }

        return repeatReminder;
    }

    private List<ReminderTime> getRepeatReminderTimes(RepeatTime repeatTime, ZoneId zoneId) {
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
        ZonedDateTime now = TimeUtils.now(zoneId);

        intervalReminderTime(now, repeatTime.getInterval(), 0, reminderTimes, true);

        if (reminderTimeAI.isNeedCreateReminderTime(repeatTime.getInterval(), 20)) {
            intervalReminderTime(now, repeatTime.getInterval(), 20, reminderTimes, false);
        }
        if (reminderTimeAI.isNeedCreateReminderTime(repeatTime.getInterval(), 60)) {
            intervalReminderTime(now, repeatTime.getInterval(), 60, reminderTimes, false);
        }
        if (reminderTimeAI.isNeedCreateReminderTime(repeatTime.getInterval(), 120)) {
            intervalReminderTime(now, repeatTime.getInterval(), 120, reminderTimes, false);
        }
    }

    private void intervalReminderTime(ZonedDateTime remindAt, Period interval, int minutes, List<ReminderTime> reminderTimes, boolean itsTime) {
        ReminderTime reminderTime = new ReminderTime();
        reminderTime.setType(ReminderTime.Type.REPEAT);
        reminderTime.setLastReminderAt(remindAt.minusMinutes(minutes).withZoneSameInstant(ZoneOffset.UTC));
        reminderTime.setDelayTime(interval);
        reminderTime.setItsTime(itsTime);
        reminderTimes.add(reminderTime);
    }

    private void addDailyReminderTimes(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = TimeUtils.now(zoneId);
        ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

        if (repeatReminder.isBefore(now)) {
            repeatReminder.plusDays(repeatTime.getInterval().getDays());
        }

        beforeHoursReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 0, reminderTimes, true);
        beforeMinutesReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 20, reminderTimes);
        beforeHoursReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 1, reminderTimes, false);
        beforeHoursReminderTime(repeatReminder, repeatTime.getInterval().getDays(), 2, reminderTimes, false);
        nightBeforeReminderTime(repeatReminder,  repeatTime.getInterval().getDays(), 0, reminderTimes);
    }

    private void addWeeklyReminderTimes(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = TimeUtils.now(zoneId);
        ZonedDateTime repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());

        beforeHoursReminderTime(repeatReminder, 7, 0, reminderTimes, true);
        beforeMinutesReminderTime(repeatReminder, 7, 20, reminderTimes);
        beforeHoursReminderTime(repeatReminder, 7, 1, reminderTimes, false);
        beforeHoursReminderTime(repeatReminder, 7, 2, reminderTimes, false);
        nightBeforeReminderTime(repeatReminder,7, 1, reminderTimes);
    }

    private void nightBeforeReminderTime(ZonedDateTime repeatAt, int days, int minusDays, List<ReminderTime> reminderTimes) {
        ReminderTime reminderTimeNightBefore = new ReminderTime();
        reminderTimeNightBefore.setType(ReminderTime.Type.REPEAT);
        reminderTimeNightBefore.setLastReminderAt(repeatAt.minusDays(days + minusDays).with(LocalTime.of(22, 0)).withZoneSameInstant(ZoneOffset.UTC));
        reminderTimeNightBefore.setDelayTime(new Period().withDays(days));
        reminderTimes.add(reminderTimeNightBefore);
    }

    private void beforeMinutesReminderTime(ZonedDateTime remindAt, int days, int minutes, List<ReminderTime> reminderTimes) {
        ReminderTime reminderTimeMinutes = new ReminderTime();
        reminderTimeMinutes.setType(ReminderTime.Type.REPEAT);
        reminderTimeMinutes.setLastReminderAt(remindAt.minusDays(days).minusMinutes(minutes).withZoneSameInstant(ZoneOffset.UTC));
        reminderTimeMinutes.setDelayTime(new Period().withDays(days));
        reminderTimes.add(reminderTimeMinutes);
    }

    private void beforeHoursReminderTime(ZonedDateTime remindAt, int days, int hours, List<ReminderTime> reminderTimes, boolean itsTime) {
        ReminderTime beforeHourReminderTime = new ReminderTime();
        beforeHourReminderTime.setType(ReminderTime.Type.REPEAT);
        beforeHourReminderTime.setLastReminderAt(remindAt.minusDays(days).minusHours(hours).withZoneSameInstant(ZoneOffset.UTC));
        beforeHourReminderTime.setDelayTime(new Period().withDays(days));
        beforeHourReminderTime.setItsTime(itsTime);
        reminderTimes.add(beforeHourReminderTime);
    }

    public static void main(String[] args) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        System.out.println(zonedDateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
    }
}
