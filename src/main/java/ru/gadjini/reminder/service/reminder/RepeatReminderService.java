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
import ru.gadjini.reminder.domain.jooq.datatype.RepeatTimeRecord;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.ai.ReminderNotificationAI;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeCreator;
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

    private TimeCreator timeCreator;

    @Autowired
    public RepeatReminderService(ReminderDao reminderDao, CompletedReminderDao completedReminderDao,
                                 ReminderNotificationService reminderNotificationService,
                                 ReminderNotificationAI reminderNotificationAI,
                                 UserReminderNotificationService userReminderNotificationService, TimeCreator timeCreator) {
        this.reminderDao = reminderDao;
        this.completedReminderDao = completedReminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderNotificationAI = reminderNotificationAI;
        this.userReminderNotificationService = userReminderNotificationService;
        this.timeCreator = timeCreator;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Reminder created = reminderDao.create(reminder);
        List<ReminderNotification> reminderNotifications = new ArrayList<>();
        for (RepeatTime repeatTime : reminder.getRepeatRemindAts()) {
            reminderNotifications.addAll(getRepeatReminderNotifications(timeCreator.withZone(repeatTime, reminder.getReceiverZoneId()), reminder.getReceiverId()));
        }
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(created.getId()));
        reminderNotificationService.create(reminderNotifications);
        reminder.setSuppressNotifications(reminderNotifications.size() == 0);

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
                reminder.getRepeatRemindAt().isEveryWeeklyTime() ||
                TimeUtils.isBigInterval(reminder.getRepeatRemindAt().getInterval())
        ) {
            return false;
        }

        return reminderNotification.isItsTime();
    }

    RemindAtCandidate getFirstRemindAt(List<RepeatTime> repeatTimes) {
        DateTime firstRemindAt = getFirstRemindAt(repeatTimes.get(0));
        int index = 0;

        if (repeatTimes.size() > 1) {
            for (int i = 1; i < repeatTimes.size(); ++i) {
                RepeatTime repeatTime = repeatTimes.get(i);
                DateTime candidate = getFirstRemindAt(repeatTime);
                LocalTime candidateTime = candidate.hasTime() ? candidate.time() : LocalTime.MIDNIGHT;
                LocalDate candidateDate = candidate.date();

                LocalTime time = firstRemindAt.hasTime() ? firstRemindAt.time() : LocalTime.MIDNIGHT;
                LocalDate date = firstRemindAt.date();
                if (ZonedDateTime.of(candidateDate, candidateTime, repeatTime.getZoneId()).isBefore(ZonedDateTime.of(date, time, firstRemindAt.getZoneId()))) {
                    firstRemindAt = candidate;
                    index = i;
                }
            }
        }

        return new RemindAtCandidate(index, firstRemindAt);
    }

    List<ReminderNotification> updateReminderNotifications(int reminderId, int receiverId, List<RepeatTime> repeatTimesInReceiverZone) {
        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> reminderNotifications = new ArrayList<>();
        for (RepeatTime repeatTimeInReceiverZone : repeatTimesInReceiverZone) {
            reminderNotifications.addAll(getRepeatReminderNotifications(repeatTimeInReceiverZone, receiverId));
        }
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);

        return reminderNotifications;
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
        RemindAtCandidate nextRemindAtCandidate = getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtsInReceiverZone(timeCreator));
        updateNextRemindAt(reminder.getId(), nextRemindAtCandidate.index, nextRemindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC), reminder.isInactive() ? UpdateSeries.NONE : UpdateSeries.RESET);
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
        }

        return new ReturnReminderResult(toReturn, returned);
    }

    public void updateNextRemindAt(int reminderId, int currRepeatIndex, DateTime nextRemindAt, UpdateSeries updateSeries) {
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
        updateValues.put(ReminderTable.TABLE.CURR_REPEAT_INDEX, currRepeatIndex);

        reminderDao.update(
                updateValues,
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
    }

    public RemindAtCandidate getNextRemindAt(DateTime remindAt, List<RepeatTime> repeatTimes) {
        DateTime nextRemindAt = getNextRemindAt(remindAt, repeatTimes.get(0));
        int index = 0;

        if (repeatTimes.size() > 1) {
            for (int i = 1; i < repeatTimes.size(); ++i) {
                RepeatTime repeatTime = repeatTimes.get(i);
                DateTime candidate = getNextRemindAt(remindAt, repeatTime);
                LocalTime candidateTime = candidate.hasTime() ? candidate.time() : LocalTime.MIDNIGHT;
                LocalDate candidateDate = candidate.date();

                LocalTime time = nextRemindAt.hasTime() ? nextRemindAt.time() : LocalTime.MIDNIGHT;
                LocalDate date = nextRemindAt.date();
                if (ZonedDateTime.of(candidateDate, candidateTime, repeatTime.getZoneId()).isBefore(ZonedDateTime.of(date, time, nextRemindAt.getZoneId()))) {
                    nextRemindAt = candidate;
                    index = i;
                }
            }
        }

        return new RemindAtCandidate(index, nextRemindAt);
    }

    private RemindAtCandidate getPrevRemindAt(DateTime remindAt, List<RepeatTime> repeatTimes) {
        DateTime prevRemindAt = getPrevRemindAt(remindAt, repeatTimes.get(0));
        int index = 0;

        if (repeatTimes.size() > 1) {
            for (int i = 1; i < repeatTimes.size(); ++i) {
                RepeatTime repeatTime = repeatTimes.get(i);
                DateTime candidate = getPrevRemindAt(remindAt, repeatTime);
                LocalTime candidateTime = candidate.hasTime() ? candidate.time() : LocalTime.MIDNIGHT;
                LocalDate candidateDate = candidate.hasTime() ? candidate.date() : candidate.date().plusDays(1);

                LocalTime time = prevRemindAt.hasTime() ? prevRemindAt.time() : LocalTime.MIDNIGHT;
                LocalDate date = prevRemindAt.hasTime() ? prevRemindAt.date() : prevRemindAt.date().plusDays(1);
                if (ZonedDateTime.of(candidateDate, candidateTime, repeatTime.getZoneId()).isAfter(ZonedDateTime.of(date, time, prevRemindAt.getZoneId()))) {
                    prevRemindAt = candidate;
                    index = i;
                }
            }
        }
        LocalTime time = prevRemindAt.hasTime() ? prevRemindAt.time() : LocalTime.MIDNIGHT;
        LocalDate date = prevRemindAt.hasTime() ? prevRemindAt.date() : prevRemindAt.date().plusDays(1);

        return ZonedDateTime.of(date, time, remindAt.getZoneId()).isBefore(timeCreator.zonedDateTimeNow(remindAt.getZoneId())) ? null : new RemindAtCandidate(index, prevRemindAt);
    }

    private DateTime getPrevRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        remindAt = remindAt.copy();

        if (repeatTime.isEveryWeeklyTime()) {
            DateTime dateTime = remindAt.copy();
            dateTime.date(dateTime.date().with(TemporalAdjusters.previous(repeatTime.getDayOfWeek())));

            return dateTime;
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
    public Reminder changeReminderTime(int reminderId, int receiverId, List<RepeatTime> repeatTimesInReceiverZone) {
        RemindAtCandidate remindAtCandidate = getFirstRemindAt(repeatTimesInReceiverZone);
        DateTime firstRemindAt = remindAtCandidate.remindAt.withZoneSameInstant(ZoneOffset.UTC);
        PGobject sqlObject = firstRemindAt.sqlObject();
        List<RepeatTime> repeatTimes = timeCreator.withZone(repeatTimesInReceiverZone, ZoneOffset.UTC);
        reminderDao.update(
                Map.ofEntries(
                        Map.entry(ReminderTable.TABLE.REPEAT_REMIND_AT, repeatTimes.stream().map(RepeatTimeRecord::new).toArray()),
                        Map.entry(ReminderTable.TABLE.INITIAL_REMIND_AT, sqlObject),
                        Map.entry(ReminderTable.TABLE.REMIND_AT, sqlObject),
                        Map.entry(ReminderTable.TABLE.CURR_REPEAT_INDEX, remindAtCandidate.index)
                ),
                ReminderTable.TABLE.ID.eq(reminderId),
                null
        );

        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> notifications = new ArrayList<>();

        for (RepeatTime repeatTimeInReceiverZone : repeatTimesInReceiverZone) {
            notifications.addAll(getRepeatReminderNotifications(repeatTimeInReceiverZone, receiverId));
        }
        notifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(notifications);

        Reminder reminder = new Reminder();
        reminder.setRepeatRemindAts(repeatTimes);
        reminder.setInitialRemindAt(firstRemindAt);
        reminder.setRemindAt(firstRemindAt);
        reminder.setCurrRepeatIndex(remindAtCandidate.getIndex());
        reminder.setSuppressNotifications(notifications.size() == 0);

        return reminder;
    }

    private boolean moveReminderNotificationToPrevPeriod(Reminder reminder) {
        RemindAtCandidate prevRemindAt = getPrevRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAts());

        if (prevRemindAt != null) {
            int id = reminder.getId();
            List<ReminderNotification> reminderNotifications = reminderNotificationService.getList(id);
            for (ReminderNotification reminderNotification : reminderNotifications) {
                if (reminderNotification.getType().equals(ReminderNotification.Type.REPEAT) && isCanMoveToPrev(reminderNotification)) {
                    ZonedDateTime prevLastRemindAt = JodaTimeUtils.minus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
                    reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), prevLastRemindAt.toLocalDateTime());
                }
            }
            updateNextRemindAt(id, prevRemindAt.getIndex(), prevRemindAt.getRemindAt(), UpdateSeries.DECREMENT);
            reminder.setRemindAt(prevRemindAt.remindAt);
            reminder.setInitialRemindAt(prevRemindAt.remindAt);

            return true;
        }

        return false;
    }

    private boolean isCanMoveToPrev(ReminderNotification reminderNotification) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow();

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
        RemindAtCandidate nextRemindAtCandidate = getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtsInReceiverZone(timeCreator));
        DateTime nextRemindAt = nextRemindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC);
        updateNextRemindAt(id, nextRemindAtCandidate.getIndex(), nextRemindAt, updateSeries);
        reminder.setRemindAt(nextRemindAt);
    }

    private boolean isNotSentYet(Reminder reminder, ReminderNotification reminderNotification) {
        if (reminder.getRepeatRemindAt().isEveryWeeklyTime() || TimeUtils.isBigInterval(reminder.getRepeatRemindAt().getInterval())) {
            return isNotSentYetForWeeklyDailyMonthlyYearlyTime(reminder, reminderNotification);
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

    private boolean isNotSentYetForWeeklyDailyMonthlyYearlyTime(Reminder reminder, ReminderNotification reminderNotification) {
        LocalDate remindAt = reminder.getRemindAt().date();

        if (reminder.getRepeatRemindAt().isEveryWeeklyTime()) {
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
        ZonedDateTime now = timeCreator.zonedDateTimeNow(repeatTime.getZoneId());

        return DateTime.of(JodaTimeUtils.plus(now, repeatTime.getInterval()));
    }

    private DateTime getDayOfWeekFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = timeCreator.zonedDateTimeNow(repeatTime.getZoneId());
            ZonedDateTime firstRemindAt = now.with(repeatTime.getTime()).with(TemporalAdjusters.nextOrSame(repeatTime.getDayOfWeek()));

            return DateTime.of(firstRemindAt);
        }
        LocalDate firstRemindAt = timeCreator.localDateNow(repeatTime.getZoneId()).with(TemporalAdjusters.nextOrSame(repeatTime.getDayOfWeek()));

        return DateTime.of(firstRemindAt, null, repeatTime.getZoneId());
    }

    private DateTime getWeeklyNextRemindAt(DateTime reminderAt, RepeatTime repeatTime) {
        ZoneId zoneId = reminderAt.getZoneId();
        LocalDate nextDate = reminderAt.date().with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));
        LocalDate now = timeCreator.localDateNow(zoneId);

        while (now.isAfter(nextDate)) {
            nextDate = nextDate.with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));
        }

        return DateTime.of(nextDate, repeatTime.getTime(), zoneId);
    }

    private DateTime getWeeklyDailyFirstRemindAt(RepeatTime repeatTime) {
        return DateTime.of(timeCreator.localDateNow(repeatTime.getZoneId()), repeatTime.getTime(), repeatTime.getZoneId());
    }

    private DateTime getWeeklyDailyMonthlyYearlyNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        LocalDate now = timeCreator.localDateNow(remindAt.getZoneId());
        LocalDate nextRemindAt = JodaTimeUtils.plus(remindAt.date(), repeatTime.getInterval());

        while (now.isAfter(nextRemindAt)) {
            nextRemindAt = JodaTimeUtils.plus(nextRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAt, repeatTime.getTime(), remindAt.getZoneId());
    }

    private DateTime getIntervalNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZoneId());
        ZonedDateTime nextRemindAt = JodaTimeUtils.plus(remindAt.toZonedDateTime(), repeatTime.getInterval());

        while (now.isAfter(nextRemindAt) || now.isEqual(nextRemindAt)) {
            nextRemindAt = JodaTimeUtils.plus(nextRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAt);
    }

    private DateTime getMonthlyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = timeCreator.zonedDateTimeNow(repeatTime.getZoneId());
            ZonedDateTime firstRemindAt = ZonedDateTime.of(
                    timeCreator.localDateNow(repeatTime.getZoneId()).withDayOfMonth(repeatTime.getDay()),
                    repeatTime.getTime(), repeatTime.getZoneId()
            );

            if (now.isAfter(firstRemindAt)) {
                firstRemindAt = firstRemindAt.withMonth(now.getMonthValue()).plusMonths(repeatTime.getInterval().getMonths());
            }

            return DateTime.of(firstRemindAt);
        }
        LocalDate now = timeCreator.localDateNow(repeatTime.getZoneId());
        LocalDate firstRemindAt = now.withDayOfMonth(repeatTime.getDay());

        if (now.isAfter(firstRemindAt)) {
            firstRemindAt = firstRemindAt.withMonth(now.getMonthValue()).plusMonths(repeatTime.getInterval().getMonths());
        }

        return DateTime.of(firstRemindAt, null, repeatTime.getZoneId());
    }

    private DateTime getYearlyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = timeCreator.zonedDateTimeNow(repeatTime.getZoneId());
            ZonedDateTime firstRemindAt = ZonedDateTime.of(
                    timeCreator.localDateNow(repeatTime.getZoneId()).withDayOfMonth(repeatTime.getDay()).withMonth(repeatTime.getMonth().getValue()),
                    repeatTime.getTime(), repeatTime.getZoneId()
            );

            if (now.isAfter(firstRemindAt)) {
                firstRemindAt = firstRemindAt.plusYears(repeatTime.getInterval().getYears());
            }

            return DateTime.of(firstRemindAt);
        }
        LocalDate now = timeCreator.localDateNow(repeatTime.getZoneId());
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

        if (repeatTime.isEveryWeeklyTime()) {
            addWeeklyReminderNotificationsWithoutTime(repeatTime, receiverId, reminderNotifications);
        } else if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
            addWeeklyMonthlyOrYearlyOrDailyReminderNotificationsWithoutTime(repeatTime, receiverId, reminderNotifications);
        } else {
            addIntervalReminderNotifications(repeatTime, receiverId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithTime(RepeatTime repeatTime, int receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.isEveryWeeklyTime()) {
            addWeeklyReminderNotifications(repeatTime, receiverId, reminderNotifications);
        } else if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
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

        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (repeatTime.getInterval().getDays() == 1 && userReminderNotification.getDays() != 0) {
                continue;
            }
            ReminderNotification reminderNotification = fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(userReminderNotification.getDays()),
                    repeatTime.getInterval(),
                    userReminderNotification.getTime() == null ? repeatReminder.toLocalTime().minusHours(userReminderNotification.getHours()).minusMinutes(userReminderNotification.getMinutes()) : userReminderNotification.getTime(),
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

    private void addWeeklyMonthlyOrYearlyOrDailyReminderNotificationsWithoutTime(RepeatTime repeatTimeInReceiverZone, int receiverId, List<ReminderNotification> reminderNotifications) {
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
        if (lastRemindAt.isAfter(timeCreator.zonedDateTimeNow())) {
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
        if (lastRemindAt.isAfter(timeCreator.zonedDateTimeNow())) {
            lastRemindAt = JodaTimeUtils.minus(lastRemindAt, period);
        }
        reminderNotification.setLastReminderAt(lastRemindAt);
        reminderNotification.setDelayTime(period);
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }

    private DateTime getNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        if (repeatTime.isEveryWeeklyTime()) {
            return getWeeklyNextRemindAt(remindAt, repeatTime);
        } else if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
            return getWeeklyDailyMonthlyYearlyNextRemindAt(remindAt, repeatTime);
        } else {
            return getIntervalNextRemindAt(remindAt, repeatTime);
        }
    }

    private DateTime getFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasDayOfWeek()) {
            return getDayOfWeekFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getDays() != 0
                || repeatTime.getInterval().getWeeks() != 0) {
            return getWeeklyDailyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getMonths() != 0) {
            return getMonthlyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getYears() != 0) {
            return getYearlyFirstRemindAt(repeatTime);
        } else {
            return getIntervalFirstRemindAt(repeatTime);
        }
    }

    public static class RemindAtCandidate {

        private int index;

        private DateTime remindAt;

        private RemindAtCandidate(int index, DateTime remindAt) {
            this.index = index;
            this.remindAt = remindAt;
        }

        public int getIndex() {
            return index;
        }

        public DateTime getRemindAt() {
            return remindAt;
        }
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
