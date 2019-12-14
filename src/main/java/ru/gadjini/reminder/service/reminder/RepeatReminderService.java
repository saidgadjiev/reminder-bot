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
        DateTime nextRemindAtInReceiverZone = getFirstRemindAt(zoneId, reminder.getRepeatRemindAt());
        reminder.setRemindAt(nextRemindAtInReceiverZone.withZoneSameInstant(ZoneOffset.UTC));

        Reminder created = reminderDao.create(reminder);
        List<ReminderTime> reminderTimes = getRepeatReminderTimes(reminder.getRepeatRemindAt(), zoneId);
        reminderTimes.forEach(reminderTime -> reminderTime.setReminderId(created.getId()));
        reminderTimeService.create(reminderTimes);

        return created;
    }

    public void updateNextRemindAt(int reminderId, DateTime nextRemindAt) {
        reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.REMIND_AT, nextRemindAt.sqlObject());
                    put(ReminderTable.TABLE.INITIAL_REMIND_AT, nextRemindAt.sqlObject());
                }},
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
    }

    public DateTime getNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        if (repeatTime.getDayOfWeek() != null) {
            return getWeeklyNextRemindAt(remindAt.getZone(), repeatTime);
        } else if (repeatTime.getInterval().getDays() > 0) {
            return getDailyNextRemindAt(remindAt, repeatTime);
        } else {
            return getIntervalNextRemindAt(remindAt, repeatTime);
        }
    }

    private DateTime getFirstRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        if (repeatTime.hasDayOfWeek()) {
            return getWeeklyNextRemindAt(zoneId, repeatTime);
        } else if (repeatTime.getInterval().getDays() > 0) {
            return getDailyFirstRemindAt(zoneId, repeatTime);
        } else {
            return getIntervalFirstRemindAt(zoneId, repeatTime);
        }
    }

    private DateTime getIntervalNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime lastRemindAt = remindAt.toZonedDateTime();
        ZonedDateTime now = ZonedDateTime.now(lastRemindAt.getZone());

        while (now.isAfter(lastRemindAt)) {
            lastRemindAt = JodaTimeUtils.plus(lastRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(lastRemindAt);
    }

    private DateTime getIntervalFirstRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.now(zoneId);

        return DateTime.of(now.plusHours(repeatTime.getInterval().getHours()).plusMinutes(repeatTime.getInterval().getMinutes()));
    }

    private DateTime getWeeklyNextRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = TimeUtils.now(zoneId);

            return DateTime.of(now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime()));
        }
        LocalDate now = LocalDate.now(zoneId);

        now = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(now);

        return DateTime.of(now, null, zoneId);
    }

    private DateTime getDailyNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        return remindAt.plusDays(repeatTime.getInterval().getDays());
    }

    private DateTime getDailyFirstRemindAt(ZoneId zoneId, RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = TimeUtils.now(zoneId);
            ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

            if (repeatReminder.isBefore(now)) {
                repeatReminder.plusDays(repeatTime.getInterval().getDays());
            }

            return DateTime.of(repeatReminder);
        }

        return DateTime.of(LocalDate.now(zoneId).plusDays(repeatTime.getInterval().getDays()), null, zoneId);
    }

    private List<ReminderTime> getRepeatReminderTimes(RepeatTime repeatTime, ZoneId zoneId) {
        if (!repeatTime.hasTime()) {
            return getRepeatReminderTimesWithoutTime(repeatTime, zoneId);
        }

        return getRepeatReminderTimesWithTime(repeatTime, zoneId);
    }

    private List<ReminderTime> getRepeatReminderTimesWithoutTime(RepeatTime repeatTime, ZoneId zoneId) {
        List<ReminderTime> reminderTimes = new ArrayList<>();

        if (repeatTime.hasDayOfWeek()) {
            addWeeklyReminderTimesWithoutTime(repeatTime, zoneId, reminderTimes);
        } else {
            addDailyReminderTimesWithoutTime(repeatTime, zoneId, reminderTimes);
        }

        return reminderTimes;
    }

    private List<ReminderTime> getRepeatReminderTimesWithTime(RepeatTime repeatTime, ZoneId zoneId) {
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
        ReminderTime reminderTime = ReminderTime.repeatTime();
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

        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, repeatTime.getInterval().getDays(), repeatTime.getTime(), reminderTimes).setItsTime(true);
        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, repeatTime.getInterval().getDays(), repeatTime.getTime().minusMinutes(20), reminderTimes);
        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, repeatTime.getInterval().getDays(), repeatTime.getTime().minusHours(1), reminderTimes);
        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, repeatTime.getInterval().getDays(), repeatTime.getTime().minusHours(2), reminderTimes);
        fixedReminderTime(repeatReminder.toLocalDate().minusDays(1), zoneId, repeatTime.getInterval().getDays(), LocalTime.of(22, 0), reminderTimes);
    }

    private void addWeeklyReminderTimes(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        ZonedDateTime now = TimeUtils.now(zoneId);
        ZonedDateTime repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());

        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, 7, repeatTime.getTime(), reminderTimes).setItsTime(true);
        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, 7, repeatTime.getTime().minusMinutes(20), reminderTimes);
        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, 7, repeatTime.getTime().minusHours(1), reminderTimes);
        fixedReminderTime(repeatReminder.toLocalDate(), zoneId, 7, repeatTime.getTime().minusHours(2), reminderTimes);
        fixedReminderTime(repeatReminder.toLocalDate().minusDays(1), zoneId, 7, LocalTime.of(22, 0), reminderTimes);
    }

    private void addWeeklyReminderTimesWithoutTime(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        LocalDate now = LocalDate.now(zoneId);
        LocalDate repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));

        fixedReminderTime(repeatReminder.minusDays(1), zoneId, 7, LocalTime.of(22, 0), reminderTimes);
        fixedReminderTime(repeatReminder, zoneId, 7, LocalTime.of(12, 0), reminderTimes);
        fixedReminderTime(repeatReminder, zoneId, 7, LocalTime.of(22, 0), reminderTimes);
    }

    private void addDailyReminderTimesWithoutTime(RepeatTime repeatTime, ZoneId zoneId, List<ReminderTime> reminderTimes) {
        LocalDate repeatReminder = LocalDate.now(zoneId);

        if (reminderTimeAI.isNeedCreateNightBeforeReminderTime(repeatTime.getInterval())) {
            fixedReminderTime(repeatReminder.minusDays(1), zoneId, repeatTime.getInterval().getDays(), LocalTime.of(22, 0), reminderTimes);
        }
        LocalTime now = LocalTime.now(zoneId);
        if (now.isBefore(LocalTime.of(12, 0))) {
            fixedReminderTime(repeatReminder.minusDays(1), zoneId, repeatTime.getInterval().getDays(), LocalTime.of(12, 0), reminderTimes);
            fixedReminderTime(repeatReminder.minusDays(1), zoneId, repeatTime.getInterval().getDays(), LocalTime.of(22, 0), reminderTimes);
        } else {
            fixedReminderTime(repeatReminder, zoneId, repeatTime.getInterval().getDays(), LocalTime.of(12, 0), reminderTimes);
            if (now.isBefore(LocalTime.of(23, 50))) {
                fixedReminderTime(repeatReminder.minusDays(1), zoneId, repeatTime.getInterval().getDays(), LocalTime.of(22, 0), reminderTimes);
            } else {
                fixedReminderTime(repeatReminder, zoneId, repeatTime.getInterval().getDays(), LocalTime.of(22, 0), reminderTimes);
            }
        }
    }

    private ReminderTime fixedReminderTime(LocalDate repeatAt, ZoneId zoneId, int repeatDays, LocalTime localTime, List<ReminderTime> reminderTimes) {
        ReminderTime reminderTime = ReminderTime.repeatTime();
        reminderTime.setLastReminderAt(ZonedDateTime.of(repeatAt.minusDays(repeatDays), localTime, zoneId).withZoneSameInstant(ZoneOffset.UTC));
        reminderTime.setDelayTime(new Period().withDays(repeatDays));
        reminderTimes.add(reminderTime);

        return reminderTime;
    }
}
