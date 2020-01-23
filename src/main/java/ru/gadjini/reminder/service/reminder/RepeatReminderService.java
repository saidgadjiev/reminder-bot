package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.CompletedReminderDao;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationAI;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepeatReminderService {

    private ReminderDao reminderDao;

    private CompletedReminderDao completedReminderDao;

    private ReminderNotificationService reminderNotificationService;

    private ReminderNotificationAI reminderNotificationAI;

    private UserReminderNotificationService userReminderNotificationService;

    @Autowired
    public RepeatReminderService(ReminderDao reminderDao, CompletedReminderDao completedReminderDao,
                                 ReminderNotificationService reminderNotificationService,
                                 ReminderNotificationAI reminderNotificationAI,
                                 UserReminderNotificationService userReminderNotificationService) {
        this.reminderDao = reminderDao;
        this.completedReminderDao = completedReminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderNotificationAI = reminderNotificationAI;
        this.userReminderNotificationService = userReminderNotificationService;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderNotification> reminderNotifications = getRepeatReminderNotifications(reminder.getRepeatRemindAtInReceiverZone(), reminder.getReceiverId());
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(created.getId()));
        reminderNotificationService.create(reminderNotifications);

        return created;
    }

    @Transactional
    public Reminder complete(int id) {
        Reminder toComplete = reminderDao.getReminder(
                ReminderTable.TABLE.as("r").ID.eq(id),
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );
        toComplete.setCurrentSeries(toComplete.getCurrentSeries() + 1);
        toComplete.setMaxSeries(Math.max(toComplete.getMaxSeries(), toComplete.getCurrentSeries()));
        toComplete.setTotalSeries(toComplete.getTotalSeries() + 1);
        completedReminderDao.create(toComplete);
        moveReminderNotificationToNextPeriod(toComplete, UpdateSeries.INCREMENT);

        return toComplete;
    }

    public boolean isNeedUpdateNextRemindAt(Reminder reminder, ReminderNotification reminderNotification) {
        if (reminder.getRepeatRemindAt().hasTime() ||
                reminder.getRepeatRemindAt().getDayOfWeek() != null ||
                reminder.getRepeatRemindAt().getInterval().getDays() != 0 ||
                reminder.getRepeatRemindAt().getInterval().getYears() != 0 ||
                reminder.getRepeatRemindAt().getInterval().getMonths() != 0
        ) {
            return false;
        }

        return reminderNotification.isItsTime();
    }

    public DateTime getFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasDayOfWeek()) {
            return getWeeklyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getDays() != 0) {
            return getDailyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getMonths() != 0) {
            return getMonthlyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getYears() != 0) {
            return getYearlyFirstRemindAt(repeatTime);
        } else {
            return getIntervalFirstRemindAt(repeatTime);
        }
    }

    public void updateReminderNotifications(int reminderId, int receiverId, RepeatTime repeatTimeInReceiverZone) {
        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> reminderNotifications = getRepeatReminderNotifications(repeatTimeInReceiverZone, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);
    }

    public List<Reminder> getOverdueRepeatReminders() {
        return reminderDao.getOverdueRepeatReminders();
    }

    @Transactional
    public Reminder skip(int id) {
        Reminder toSkip = reminderDao.getReminder(
                ReminderTable.TABLE.as("r").ID.eq(id),
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );
        toSkip.setCurrentSeries(0);
        moveReminderNotificationToNextPeriod(toSkip, UpdateSeries.RESET);

        return toSkip;
    }

    public void autoSkip(Reminder reminder) {
        DateTime nextRemindAt = getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtInReceiverZone()).withZoneSameInstant(ZoneOffset.UTC);
        updateNextRemindAt(reminder.getId(), nextRemindAt, reminder.isInactive() ? UpdateSeries.NONE : UpdateSeries.RESET);
    }

    public Reminder enableCountSeries(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.COUNT_SERIES, true, ReminderTable.TABLE.CURRENT_SERIES, 0, ReminderTable.TABLE.MAX_SERIES, 0),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    public Reminder disableCountSeries(int reminderId) {
        return reminderDao.update(
                Map.of(ReminderTable.TABLE.COUNT_SERIES, false),
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping()
                        .setCreatorMapping(new Mapping())
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
        );
    }

    @Transactional
    public ReturnReminderResult returnReminder(int id) {
        Reminder toReturn = reminderDao.getReminder(
                ReminderTable.TABLE.as("r").ID.eq(id),
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );
        boolean returned = moveReminderNotificationToPrevPeriod(toReturn);

        if (returned) {
            toReturn.setCurrentSeries(Math.max(0, toReturn.getCurrentSeries() - 1));
            toReturn.setTotalSeries(Math.max(0, toReturn.getTotalSeries() - 1));
        }

        return new ReturnReminderResult(toReturn, returned);
    }

    public void updateNextRemindAt(int reminderId, DateTime nextRemindAt, UpdateSeries updateSeries) {
        Map<Field<?>, Object> updateValues = new HashMap<>();

        switch (updateSeries) {
            case NONE:
                break;
            case RESET:
                updateValues.put(ReminderTable.TABLE.CURRENT_SERIES, 0);
                break;
            case INCREMENT:
                updateValues.put(ReminderTable.TABLE.CURRENT_SERIES, ReminderTable.TABLE.CURRENT_SERIES.plus(1));
                updateValues.put(ReminderTable.TABLE.MAX_SERIES, DSL.greatest(ReminderTable.TABLE.MAX_SERIES, ReminderTable.TABLE.CURRENT_SERIES.plus(1)));
                updateValues.put(ReminderTable.TABLE.TOTAL_SERIES, ReminderTable.TABLE.TOTAL_SERIES.plus(1));
                break;
            case DECREMENT:
                updateValues.put(ReminderTable.TABLE.CURRENT_SERIES, DSL.greatest(0, ReminderTable.TABLE.CURRENT_SERIES.minus(1)));
                break;
        }

        updateValues.put(ReminderTable.TABLE.REMIND_AT, nextRemindAt.sqlObject());
        updateValues.put(ReminderTable.TABLE.INITIAL_REMIND_AT, nextRemindAt.sqlObject());

        reminderDao.update(
                updateValues,
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
    }

    public DateTime getNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        if (repeatTime.getDayOfWeek() != null) {
            return getWeeklyNextRemindAt(remindAt, repeatTime);
        } else if (repeatTime.getInterval().getDays() != 0 || repeatTime.getInterval().getYears() != 0 || repeatTime.getInterval().getMonths() != 0) {
            return getDailyMonthlyYearlyNextRemindAt(remindAt, repeatTime);
        } else {
            return getIntervalNextRemindAt(remindAt, repeatTime);
        }
    }

    private DateTime getPrevRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        if (repeatTime.getDayOfWeek() != null) {
            return remindAt.minusDays(7);
        } else {
            if (remindAt.hasTime()) {
                ZonedDateTime prevRemindAt = JodaTimeUtils.minus(remindAt.toZonedDateTime(), repeatTime.getInterval());

                return DateTime.of(prevRemindAt);
            }

            LocalDate prevRemindAt = JodaTimeUtils.minus(remindAt.date(), repeatTime.getInterval());

            return DateTime.of(prevRemindAt, null, remindAt.getZoneId());
        }
    }

    @Transactional
    public Reminder changeReminderTime(int reminderId, int receiverId, RepeatTime repeatTimeInReceiverZone) {
        DateTime nextRemindAt = getFirstRemindAt(repeatTimeInReceiverZone).withZoneSameInstant(ZoneOffset.UTC);
        PGobject sqlObject = nextRemindAt.sqlObject();
        RepeatTime repeatTime = repeatTimeInReceiverZone.withZone(ZoneOffset.UTC);
        reminderDao.update(
                Map.ofEntries(
                        Map.entry(ReminderTable.TABLE.REPEAT_REMIND_AT, repeatTime.sqlObject()),
                        Map.entry(ReminderTable.TABLE.INITIAL_REMIND_AT, sqlObject),
                        Map.entry(ReminderTable.TABLE.REMIND_AT, sqlObject)
                ),
                ReminderTable.TABLE.ID.eq(reminderId),
                null
        );

        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> reminderNotifications = getRepeatReminderNotifications(repeatTimeInReceiverZone, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);

        Reminder reminder = new Reminder();
        reminder.setId(reminderId);
        reminder.setReceiverId(receiverId);
        reminder.setRepeatRemindAt(repeatTime);
        reminder.setInitialRemindAt(nextRemindAt);
        reminder.setRemindAt(nextRemindAt);

        return reminder;
    }

    private boolean moveReminderNotificationToPrevPeriod(Reminder reminder) {
        if (isCanMoveToPrev(reminder)) {
            int id = reminder.getId();
            List<ReminderNotification> reminderNotifications = reminderNotificationService.getList(id);
            for (ReminderNotification reminderNotification : reminderNotifications) {
                if (reminderNotification.getType().equals(ReminderNotification.Type.REPEAT) && isCanMoveToPrev(reminderNotification)) {
                    ZonedDateTime prevLastRemindAt = JodaTimeUtils.minus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
                    reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), prevLastRemindAt.toLocalDateTime());
                }
            }
            DateTime prevRemindAt = getPrevRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAt());
            updateNextRemindAt(id, prevRemindAt, UpdateSeries.DECREMENT);
            reminder.setRemindAt(prevRemindAt);

            return true;
        }

        return false;
    }

    private boolean isCanMoveToPrev(Reminder reminder) {
        if (reminder.getRepeatRemindAt().getDayOfWeek() != null ||
                reminder.getRepeatRemindAt().getInterval().getDays() > 0 ||
                reminder.getRepeatRemindAt().getInterval().getMonths() > 0 ||
                reminder.getRepeatRemindAt().getInterval().getYears() > 0) {
            LocalDate now = LocalDate.now();
            LocalDate prevDate;

            if (reminder.getRepeatRemindAt().getDayOfWeek() != null) {
                prevDate = reminder.getRemindAt().date().minusDays(7);
            } else {
                prevDate = JodaTimeUtils.minus(reminder.getRemindAt().date(), reminder.getRepeatRemindAt().getInterval());
            }

            return now.isBefore(prevDate) || now.isEqual(prevDate);
        } else {
            ZonedDateTime now = TimeUtils.nowZoned();
            ZonedDateTime prevRemindAt = JodaTimeUtils.minus(reminder.getRemindAt().toZonedDateTime(), reminder.getRepeatRemindAt().getInterval());

            return now.isBefore(prevRemindAt);
        }
    }

    private boolean isCanMoveToPrev(ReminderNotification reminderNotification) {
        ZonedDateTime now = ZonedDateTime.now();

        return now.isBefore(reminderNotification.getLastReminderAt());
    }

    private void moveReminderNotificationToNextPeriod(Reminder reminder, UpdateSeries updateSeries) {
        int id = reminder.getId();
        List<ReminderNotification> reminderNotifications = reminderNotificationService.getList(id);
        for (ReminderNotification reminderNotification : reminderNotifications) {
            if (reminderNotification.getType().equals(ReminderNotification.Type.REPEAT)) {
                if (isNotSentYet(reminder, reminderNotification)) {
                    ZonedDateTime nextLastRemindAt = JodaTimeUtils.plus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
                    reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), nextLastRemindAt.toLocalDateTime());
                }
            } else {
                reminderNotificationService.deleteReminderNotification(reminderNotification.getId());
            }
        }
        DateTime nextRemindAt = getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtInReceiverZone()).withZoneSameInstant(ZoneOffset.UTC);
        updateNextRemindAt(id, nextRemindAt, updateSeries);
        reminder.setRemindAt(nextRemindAt);
    }

    private boolean isNotSentYet(Reminder reminder, ReminderNotification reminderNotification) {
        if (reminder.getRepeatRemindAt().getDayOfWeek() != null ||
                reminder.getRepeatRemindAt().getInterval().getDays() > 0 ||
                reminder.getRepeatRemindAt().getInterval().getMonths() > 0 ||
                reminder.getRepeatRemindAt().getInterval().getYears() > 0) {
            return isNotSentYetForDailyMonthlyYearlyTime(reminder, reminderNotification);
        } else {
            return isNotSendYetForIntervalTime(reminder, reminderNotification);
        }
    }

    private boolean isNotSendYetForIntervalTime(Reminder reminder, ReminderNotification reminderNotification) {
        ZonedDateTime left = reminder.getRemindAt().toZonedDateTime();

        if (left.isAfter(reminderNotification.getLastReminderAt())) {
            return true;
        }

        return left.isEqual(reminderNotification.getLastReminderAt());
    }

    private boolean isNotSentYetForDailyMonthlyYearlyTime(Reminder reminder, ReminderNotification reminderNotification) {
        LocalDate remindAt = reminder.getRemindAt().date();

        if (reminder.getRepeatRemindAt().getDayOfWeek() != null) {
            remindAt = remindAt.minusDays(7);
        } else {
            remindAt = JodaTimeUtils.minus(remindAt, reminderNotification.getDelayTime());
        }

        if (remindAt.isAfter(reminderNotification.getLastReminderAt().toLocalDate())) {
            return true;
        }

        return remindAt.isEqual(reminderNotification.getLastReminderAt().toLocalDate());
    }

    private DateTime getIntervalFirstRemindAt(RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.now(repeatTime.getZoneId());

        return DateTime.of(JodaTimeUtils.plus(now, repeatTime.getInterval()));
    }

    private DateTime getWeeklyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = TimeUtils.now(repeatTime.getZoneId()).with(repeatTime.getTime());

            if (!now.getDayOfWeek().equals(repeatTime.getDayOfWeek())) {
                now = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));
            }

            return DateTime.of(now);
        }
        LocalDate now = LocalDate.now(repeatTime.getZoneId());

        if (now.getDayOfWeek().equals(repeatTime.getDayOfWeek())) {
            DateTime.of(now, null, repeatTime.getZoneId());
        }
        now = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(now);

        return DateTime.of(now, null, repeatTime.getZoneId());
    }

    private DateTime getWeeklyNextRemindAt(DateTime reminderAt, RepeatTime repeatTime) {
        ZoneId zoneId = reminderAt.getZoneId();
        LocalDate nextDate = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(reminderAt.date());
        LocalDate now = LocalDate.now(zoneId);

        while (now.isAfter(nextDate)) {
            nextDate = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(nextDate);
        }

        return DateTime.of(nextDate, repeatTime.getTime(), zoneId);
    }

    private DateTime getDailyFirstRemindAt(RepeatTime repeatTime) {
        return DateTime.of(LocalDate.now(repeatTime.getZoneId()), repeatTime.getTime(), repeatTime.getZoneId());
    }

    private DateTime getDailyMonthlyYearlyNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        LocalDate now = LocalDate.now(remindAt.getZoneId());
        LocalDate nextRemindAt = JodaTimeUtils.plus(remindAt.date(), repeatTime.getInterval());

        while (now.isAfter(nextRemindAt)) {
            nextRemindAt = JodaTimeUtils.plus(nextRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAt, null, remindAt.getZoneId());
    }

    private DateTime getIntervalNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.now(remindAt.getZoneId());
        ZonedDateTime nextRemindAt = JodaTimeUtils.plus(remindAt.toZonedDateTime(), repeatTime.getInterval());

        while (now.isAfter(nextRemindAt) || now.isEqual(nextRemindAt)) {
            nextRemindAt = JodaTimeUtils.plus(nextRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAt);
    }

    private DateTime getMonthlyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = TimeUtils.now(repeatTime.getZoneId());
            ZonedDateTime firstRemindAt = ZonedDateTime.of(
                    LocalDate.now(repeatTime.getZoneId()).withDayOfMonth(repeatTime.getDay()),
                    repeatTime.getTime(), repeatTime.getZoneId()
            );

            if (now.isAfter(firstRemindAt)) {
                firstRemindAt = firstRemindAt.withMonth(now.getMonthValue()).plusMonths(repeatTime.getInterval().getMonths());
            }

            return DateTime.of(firstRemindAt);
        }
        LocalDate now = LocalDate.now(repeatTime.getZoneId());
        LocalDate firstRemindAt = now.withDayOfMonth(repeatTime.getDay());

        if (now.isAfter(firstRemindAt)) {
            firstRemindAt = firstRemindAt.withMonth(now.getMonthValue()).plusMonths(repeatTime.getInterval().getMonths());
        }

        return DateTime.of(firstRemindAt, null, repeatTime.getZoneId());
    }

    private DateTime getYearlyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = TimeUtils.now(repeatTime.getZoneId());
            ZonedDateTime firstRemindAt = ZonedDateTime.of(
                    LocalDate.now(repeatTime.getZoneId()).withDayOfMonth(repeatTime.getDay()).withMonth(repeatTime.getMonth().getValue()),
                    repeatTime.getTime(), repeatTime.getZoneId()
            );

            if (now.isAfter(firstRemindAt)) {
                firstRemindAt = firstRemindAt.plusYears(repeatTime.getInterval().getYears());
            }

            return DateTime.of(firstRemindAt);
        }
        LocalDate now = LocalDate.now(repeatTime.getZoneId());
        LocalDate firstRemindAt = now.withDayOfMonth(repeatTime.getDay()).withMonth(repeatTime.getMonth().getValue());

        if (now.isAfter(firstRemindAt)) {
            firstRemindAt = firstRemindAt.plusYears(repeatTime.getInterval().getYears());
        }

        return DateTime.of(firstRemindAt, null, repeatTime.getZoneId());
    }

    private List<ReminderNotification> getRepeatReminderNotifications(RepeatTime repeatTime, int receiverId) {
        if (!repeatTime.hasTime()) {
            return getRepeatReminderNotificationsWithoutTime(repeatTime, receiverId);
        }

        return getRepeatReminderNotificationsWithTime(repeatTime, receiverId);
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithoutTime(RepeatTime repeatTime, int receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.hasDayOfWeek()) {
            addWeeklyReminderNotificationsWithoutTime(repeatTime, receiverId, reminderNotifications);
        } else if (repeatTime.getInterval().getDays() > 0 || repeatTime.getInterval().getYears() > 0 || repeatTime.getInterval().getMonths() > 0) {
            addMonthlyOrYearlyOrDailyReminderNotificationsWithoutTime(repeatTime, receiverId, reminderNotifications);
        } else {
            addIntervalReminderNotifications(repeatTime, receiverId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithTime(RepeatTime repeatTime, int receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.getDayOfWeek() != null) {
            addWeeklyReminderNotifications(repeatTime, receiverId, reminderNotifications);
        } else if (repeatTime.getInterval().getDays() > 0 || repeatTime.getInterval().getYears() > 0 || repeatTime.getInterval().getMonths() > 0) {
            addYearlyOrMonthlyOrDailyReminderNotifications(repeatTime, receiverId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private void addIntervalReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = getFirstRemindAt(repeatTime).withZoneSameInstant(ZoneOffset.UTC).toZonedDateTime();

        intervalReminderNotification(now, repeatTime.getInterval(), reminderNotifications).setItsTime(true);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (offsetTime.getTime() == null && reminderNotificationAI.isNeedCreateReminderNotification(repeatTime.getInterval(), offsetTime)) {
                intervalReminderNotification(now.minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()), repeatTime.getInterval(), reminderNotifications).setCustom(true);
            }
        }
    }

    private void addYearlyOrMonthlyOrDailyReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime repeatReminder = getFirstRemindAt(repeatTime).withZoneSameInstant(ZoneOffset.UTC).toZonedDateTime();

        fixedReminderNotification(repeatReminder.toLocalDate(), repeatTime.getInterval(), repeatReminder.toLocalTime(), reminderNotifications).setItsTime(true);
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (repeatTime.getInterval().getDays() == 1 && offsetTime.getDays() != 0) {
                continue;
            }
            ReminderNotification reminderNotification = fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    repeatTime.getInterval(),
                    offsetTime.getTime() == null ? repeatReminder.toLocalTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            );
            reminderNotification.setCustom(true);
        }
    }

    private void addWeeklyReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime repeatReminder = getFirstRemindAt(repeatTime).withZoneSameInstant(ZoneOffset.UTC).toZonedDateTime();

        Period repeatPeriod = new Period().withDays(7);
        fixedReminderNotification(repeatReminder.toLocalDate(), repeatPeriod, repeatReminder.toLocalTime(), reminderNotifications).setItsTime(true);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    repeatPeriod,
                    offsetTime.getTime() == null ? repeatReminder.toLocalTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            ).setCustom(true);
        }
    }

    private void addMonthlyOrYearlyOrDailyReminderNotificationsWithoutTime(RepeatTime repeatTimeInReceiverZone, int receiverId, List<ReminderNotification> reminderNotifications) {
        LocalDate repeatReminder = getFirstRemindAt(repeatTimeInReceiverZone).date();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (repeatTimeInReceiverZone.getInterval().getDays() == 1 && offsetTime.getDays() != 0) {
                continue;
            }
            fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), repeatTimeInReceiverZone.getInterval(), offsetTime.getTime(), reminderNotifications).setCustom(true);
        }
    }

    private void addWeeklyReminderNotificationsWithoutTime(RepeatTime repeatTimeInReceiverZone, int receiverId, List<ReminderNotification> reminderNotifications) {
        LocalDate repeatReminder = getFirstRemindAt(repeatTimeInReceiverZone).date();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        Period repeatPeriod = new Period().withDays(7);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), repeatPeriod, offsetTime.getTime(), reminderNotifications).setCustom(true);
        }
    }

    private ReminderNotification intervalReminderNotification(ZonedDateTime lastRemindAt, Period interval, List<ReminderNotification> reminderNotifications) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        if (lastRemindAt.isAfter(ZonedDateTime.now())) {
            lastRemindAt = JodaTimeUtils.minus(lastRemindAt, interval);
        }
        reminderNotification.setLastReminderAt(lastRemindAt);
        reminderNotification.setDelayTime(interval);
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }

    private ReminderNotification fixedReminderNotification(LocalDate repeatAt, Period period, LocalTime localTime, List<ReminderNotification> reminderNotifications) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        ZonedDateTime lastRemindAt = ZonedDateTime.of(repeatAt, localTime, ZoneOffset.UTC);
        if (lastRemindAt.isAfter(ZonedDateTime.now())) {
            lastRemindAt = JodaTimeUtils.minus(lastRemindAt, period);
        }
        reminderNotification.setLastReminderAt(lastRemindAt);
        reminderNotification.setDelayTime(period);
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }

    public static class ReturnReminderResult {

        private Reminder reminder;

        private boolean returned;

        private ReturnReminderResult(Reminder reminder, boolean returned) {
            this.reminder = reminder;
            this.returned = returned;
        }

        public Reminder getReminder() {
            return reminder;
        }

        public boolean isReturned() {
            return returned;
        }
    }

    public enum UpdateSeries {

        NONE,

        RESET,

        INCREMENT,

        DECREMENT
    }
}
