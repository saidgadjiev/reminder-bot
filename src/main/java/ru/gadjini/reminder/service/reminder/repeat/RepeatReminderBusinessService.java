package ru.gadjini.reminder.service.reminder.repeat;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.CompletedReminderDao;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepeatReminderBusinessService {

    private ReminderDao reminderDao;

    private CompletedReminderDao completedReminderDao;

    private DateTimeService timeCreator;

    private ReminderNotificationService reminderNotificationService;

    @Autowired
    public RepeatReminderBusinessService(ReminderDao reminderDao, CompletedReminderDao completedReminderDao,
                                         DateTimeService timeCreator, ReminderNotificationService reminderNotificationService) {
        this.reminderDao = reminderDao;
        this.completedReminderDao = completedReminderDao;
        this.timeCreator = timeCreator;
        this.reminderNotificationService = reminderNotificationService;
    }

    @Transactional
    public ReminderActionResult complete(int id) {
        Reminder toComplete = reminderDao.getReminder(
                ReminderTable.TABLE.as("r").ID.eq(id),
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );
        if (toComplete.isRepeatableWithTime()) {
            completedReminderDao.create(toComplete);
        }

        ActionResult nextResult = moveReminderNotificationToNextPeriod(toComplete, UpdateSeries.INCREMENT, true);
        if (nextResult == ActionResult.CURR_SERIES_TO_COMPLETE_CHANGED) {
            toComplete.setCurrSeriesToComplete(Math.max(0, toComplete.getCurrSeriesToComplete() - 1));
        } else if (nextResult == ActionResult.COMPLETED) {
            toComplete.setTotalSeries(toComplete.getTotalSeries() + 1);
            toComplete.setCurrentSeries(toComplete.getCurrentSeries() + 1);
            toComplete.setMaxSeries(Math.max(toComplete.getMaxSeries(), toComplete.getCurrentSeries()));

            toComplete.setCurrSeriesToComplete(toComplete.getRepeatRemindAt().getSeriesToComplete());
        }

        return new ReminderActionResult(toComplete, nextResult);
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

        moveReminderNotificationToNextPeriod(toSkip, UpdateSeries.RESET, false);
        if (toSkip.getRepeatRemindAt().hasSeriesToComplete()) {
            toSkip.setCurrSeriesToComplete(toSkip.getRepeatRemindAt().getSeriesToComplete());
        }

        return toSkip;
    }

    @Transactional
    public ReminderActionResult returnReminder(int id) {
        Reminder toReturn = reminderDao.getReminder(
                ReminderTable.TABLE.as("r").ID.eq(id),
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_NAME)))
                        .setCreatorMapping(new Mapping())
        );

        ActionResult returned = moveReminderToPrevPeriod(toReturn);

        if (returned == ActionResult.RETURNED) {
            if (toReturn.getCurrentSeries() > 0) {
                toReturn.setTotalSeries(Math.max(0, toReturn.getTotalSeries() - 1));
                toReturn.setMaxSeries(Math.max(0, toReturn.getMaxSeries() - 1));
            }
            toReturn.setCurrentSeries(Math.max(0, toReturn.getCurrentSeries() - 1));
            toReturn.setCurrSeriesToComplete(toReturn.getRepeatRemindAt().hasSeriesToComplete() ? 1 : null);
        } else if (returned == ActionResult.CURR_SERIES_TO_COMPLETE_CHANGED) {
            toReturn.setCurrSeriesToComplete(Math.min(toReturn.getRepeatRemindAt().getSeriesToComplete(), toReturn.getCurrSeriesToComplete() + 1));
        }

        return new ReminderActionResult(toReturn, returned);
    }

    @Transactional
    public void autoSkip(Reminder reminder) {
        createOverdueReminder(reminder);
        RemindAtCandidate nextRemindAtCandidate = getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtsInReceiverZone(timeCreator), reminder.getCurrRepeatIndex());
        updateNextRemindAtAndSeries(reminder.getId(), reminder.isInactive() ? UpdateSeries.NONE : UpdateSeries.RESET,
                nextRemindAtCandidate.currentSeriesToComplete, nextRemindAtCandidate.index,
                nextRemindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC));
    }

    public RemindAtCandidate getNextRemindAt(DateTime remindAt, List<RepeatTime> repeatTimes, int currentIndex) {
        DateTime nextRemindAt = getNextRemindAt(remindAt, repeatTimes.get(currentIndex), repeatTimes.get(0));
        int index = 0;
        Integer currentSeriesToComplete = repeatTimes.get(0).getSeriesToComplete();

        if (repeatTimes.size() > 1) {
            for (int i = 1; i < repeatTimes.size(); ++i) {
                RepeatTime repeatTime = repeatTimes.get(i);
                DateTime candidate = getNextRemindAt(remindAt, repeatTimes.get(currentIndex), repeatTime);
                if (isMoreAppropriateNextRemindAtCandidate(candidate, nextRemindAt)) {
                    nextRemindAt = candidate;
                    index = i;
                    currentSeriesToComplete = repeatTime.getSeriesToComplete();
                }
            }
        }

        return new RemindAtCandidate(index, nextRemindAt, currentSeriesToComplete);
    }

    public void updateNextRemindAtAndSeries(int reminderId, UpdateSeries updateSeries, Integer seriesToComplete, int currRepeatIndex, DateTime nextRemindAt) {
        updateSeries(reminderId, updateSeries, seriesToComplete, Map.of(ReminderTable.TABLE.CURR_REPEAT_INDEX, currRepeatIndex, ReminderTable.TABLE.REMIND_AT, nextRemindAt.sqlObject(),
                ReminderTable.TABLE.INITIAL_REMIND_AT, nextRemindAt.sqlObject()));
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

    public RepeatReminderBusinessService.RemindAtCandidate getFirstRemindAt(List<RepeatTime> repeatTimes) {
        DateTime firstRemindAt = getFirstRemindAt(repeatTimes.get(0));
        int index = 0;
        Integer currentSeriesToComplete = repeatTimes.get(0).getSeriesToComplete();

        if (firstRemindAt != null && repeatTimes.size() > 1) {
            for (int i = 1; i < repeatTimes.size(); ++i) {
                RepeatTime repeatTime = repeatTimes.get(i);
                DateTime candidate = getFirstRemindAt(repeatTime);
                if (isMoreAppropriateFirstRemindAtCandidate(candidate, firstRemindAt)) {
                    firstRemindAt = candidate;
                    index = i;
                }
            }
        }

        return new RepeatReminderBusinessService.RemindAtCandidate(index, firstRemindAt, currentSeriesToComplete);
    }

    DateTime getFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasDayOfWeek()) {
            return getDayOfWeekFirstRemindAt(repeatTime);
        } else if (repeatTime.hasInterval()) {
            if (repeatTime.getInterval().getDays() != 0
                    || repeatTime.getInterval().getWeeks() != 0) {
                return getWeeklyDailyFirstRemindAt(repeatTime);
            } else if (repeatTime.getInterval().getMonths() != 0) {
                return getMonthlyFirstRemindAt(repeatTime);
            } else if (repeatTime.getInterval().getYears() != 0) {
                return getYearlyFirstRemindAt(repeatTime);
            } else {
                return getIntervalFirstRemindAt(repeatTime);
            }
        } else {
            return null;
        }
    }

    private void createOverdueReminder(Reminder reminder) {
        Reminder toCreate = new Reminder(reminder);
        toCreate.setRepeatRemindAts(null);
        toCreate.setCurrRepeatIndex(null);
        toCreate.setCurrSeriesToComplete(null);
        toCreate.setSuppressNotifications(true);
        reminderDao.create(toCreate);
    }

    private void updateSeriesToComplete(int reminderId, UpdateSeries updateSeries) {
        Map<Field<?>, Object> updateValues = new HashMap<>();

        switch (updateSeries) {
            case INCREMENT:
                updateValues.put(ReminderTable.TABLE.CURR_SERIES_TO_COMPLETE, DSL.greatest(0, ReminderTable.TABLE.CURR_SERIES_TO_COMPLETE.minus(1)));
                break;
            case DECREMENT:
                updateValues.put(ReminderTable.TABLE.CURR_SERIES_TO_COMPLETE, DSL.greatest(0, ReminderTable.TABLE.CURR_SERIES_TO_COMPLETE.plus(1)));
                break;
        }

        reminderDao.update(
                updateValues,
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
    }

    private void updateSeries(int reminderId, UpdateSeries updateSeries, Integer seriesToComplete, Map<Field<?>, Object> additionalUpdateValues) {
        Map<Field<?>, Object> updateValues = new HashMap<>();

        switch (updateSeries) {
            case NONE:
                break;
            case RESET:
                updateValues.put(ReminderTable.TABLE.CURRENT_SERIES, 0);
                updateValues.put(ReminderTable.TABLE.CURR_SERIES_TO_COMPLETE, seriesToComplete);
                break;
            case INCREMENT:
                updateValues.put(ReminderTable.TABLE.CURRENT_SERIES, ReminderTable.TABLE.CURRENT_SERIES.plus(1));
                updateValues.put(ReminderTable.TABLE.MAX_SERIES, DSL.greatest(ReminderTable.TABLE.MAX_SERIES, ReminderTable.TABLE.CURRENT_SERIES.plus(1)));
                updateValues.put(ReminderTable.TABLE.TOTAL_SERIES, ReminderTable.TABLE.TOTAL_SERIES.plus(1));
                updateValues.put(ReminderTable.TABLE.CURR_SERIES_TO_COMPLETE, seriesToComplete);
                break;
            case DECREMENT:
                updateValues.put(ReminderTable.TABLE.CURRENT_SERIES, DSL.greatest(0, ReminderTable.TABLE.CURRENT_SERIES.minus(1)));
                updateValues.put(ReminderTable.TABLE.TOTAL_SERIES, DSL.field("CASE WHEN current_series > 0 THEN GREATEST(0, total_series - 1) ELSE total_series END"));
                updateValues.put(ReminderTable.TABLE.MAX_SERIES, DSL.field("CASE WHEN current_series > 0 THEN GREATEST(0, max_series - 1) ELSE max_series END"));
                updateValues.put(ReminderTable.TABLE.CURR_SERIES_TO_COMPLETE, seriesToComplete);

                break;
        }

        if (additionalUpdateValues != null) {
            updateValues.putAll(additionalUpdateValues);
        }

        reminderDao.update(
                updateValues,
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
    }

    private boolean isMoreAppropriateFirstRemindAtCandidate(DateTime candidate, DateTime currentFirstRemindAt) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(candidate.getZoneId());
        ZonedDateTime candidateZoned = ZonedDateTime.of(candidate.date(), candidate.hasTime() ? candidate.time() : LocalTime.MIDNIGHT, candidate.getZoneId());
        ZonedDateTime firstRemindAtZoned = ZonedDateTime.of(currentFirstRemindAt.date(), currentFirstRemindAt.hasTime() ? currentFirstRemindAt.time() : LocalTime.MIDNIGHT, currentFirstRemindAt.getZoneId());
        if (firstRemindAtZoned.isBefore(now)) {
            return candidateZoned.isAfter(now);
        }
        if (candidateZoned.isBefore(now)) {
            return false;
        }

        return candidateZoned.isBefore(firstRemindAtZoned);
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

    private DateTime getWeeklyDailyFirstRemindAt(RepeatTime repeatTime) {
        return DateTime.of(timeCreator.localDateNow(repeatTime.getZoneId()), repeatTime.getTime(), repeatTime.getZoneId());
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

    private ActionResult moveReminderNotificationToNextPeriod(Reminder reminder, UpdateSeries updateSeries, boolean isCompleteAction) {
        if (reminder.isRepeatableWithoutTime()) {
            updateSeries(reminder.getId(), updateSeries, null, Collections.emptyMap());
            return ActionResult.COMPLETED;
        }
        if (isCompleteAction && reminder.getRepeatRemindAt().hasSeriesToComplete() && reminder.getCurrSeriesToComplete() - 1 > 0) {
            updateSeriesToComplete(reminder.getId(), UpdateSeries.INCREMENT);
            return ActionResult.CURR_SERIES_TO_COMPLETE_CHANGED;
        }
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
        RemindAtCandidate nextRemindAtCandidate = getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtsInReceiverZone(timeCreator), reminder.getCurrRepeatIndex());
        DateTime nextRemindAt = nextRemindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC);
        updateNextRemindAtAndSeries(id, updateSeries, nextRemindAtCandidate.getCurrentSeriesToComplete(), nextRemindAtCandidate.getIndex(), nextRemindAt);
        reminder.setRemindAt(nextRemindAt);
        reminder.setCurrRepeatIndex(nextRemindAtCandidate.getIndex());

        return isCompleteAction ? ActionResult.COMPLETED : ActionResult.SKIPPED;
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

    private ActionResult moveReminderToPrevPeriod(Reminder reminder) {
        if (reminder.isRepeatableWithoutTime()) {
            updateSeries(reminder.getId(), UpdateSeries.DECREMENT, null, Collections.emptyMap());
            return ActionResult.RETURNED;
        }
        if (reminder.getRepeatRemindAt().hasSeriesToComplete() && reminder.getCurrSeriesToComplete() + 1 <= reminder.getRepeatRemindAt().getSeriesToComplete()) {
            updateSeriesToComplete(reminder.getId(), UpdateSeries.DECREMENT);
            return ActionResult.CURR_SERIES_TO_COMPLETE_CHANGED;
        }
        RemindAtCandidate prevRemindAt = getPrevRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAts(), reminder.getCurrRepeatIndex());

        if (prevRemindAt != null) {
            int id = reminder.getId();
            List<ReminderNotification> reminderNotifications = reminderNotificationService.getList(id);
            for (ReminderNotification reminderNotification : reminderNotifications) {
                if (reminderNotification.getType().equals(ReminderNotification.Type.REPEAT) && isCanMoveToPrev(reminderNotification)) {
                    ZonedDateTime prevLastRemindAt = JodaTimeUtils.minus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
                    reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), prevLastRemindAt.toLocalDateTime());
                }
            }
            updateNextRemindAtAndSeries(id, UpdateSeries.DECREMENT, prevRemindAt.getCurrentSeriesToComplete() == null ? null : 1, prevRemindAt.getIndex(), prevRemindAt.getRemindAt());
            reminder.setCurrRepeatIndex(prevRemindAt.getIndex());
            reminder.setRemindAt(prevRemindAt.remindAt);
            reminder.setInitialRemindAt(prevRemindAt.remindAt);

            return ActionResult.RETURNED;
        }

        return ActionResult.NOT_RETURNED;
    }

    private RemindAtCandidate getPrevRemindAt(DateTime remindAt, List<RepeatTime> repeatTimes, int currIndex) {
        DateTime prevRemindAt = getPrevRemindAt(remindAt, repeatTimes.get(currIndex), repeatTimes.get(0));
        int index = 0;
        Integer currentSeriesToComplete = repeatTimes.get(0).getSeriesToComplete();

        if (repeatTimes.size() > 1) {
            for (int i = 1; i < repeatTimes.size(); ++i) {
                RepeatTime repeatTime = repeatTimes.get(i);
                DateTime candidate = getPrevRemindAt(remindAt, repeatTimes.get(currIndex), repeatTime);
                if (isMoreAppropriatePrevRemindAtCandidate(candidate, prevRemindAt)) {
                    prevRemindAt = candidate;
                    index = i;
                    currentSeriesToComplete = repeatTime.getSeriesToComplete();
                }
            }
        }
        LocalTime time = prevRemindAt.hasTime() ? prevRemindAt.time() : LocalTime.MIDNIGHT;
        LocalDate date = prevRemindAt.hasTime() ? prevRemindAt.date() : prevRemindAt.date().plusDays(1);

        return ZonedDateTime.of(date, time, remindAt.getZoneId()).isBefore(timeCreator.zonedDateTimeNow(remindAt.getZoneId())) ? null : new RemindAtCandidate(index, prevRemindAt, currentSeriesToComplete);
    }

    private boolean isMoreAppropriatePrevRemindAtCandidate(DateTime candidate, DateTime prevRemindAt) {
        LocalTime candidateTime = candidate.hasTime() ? candidate.time() : LocalTime.MIDNIGHT;
        LocalDate candidateDate = candidate.hasTime() ? candidate.date() : candidate.date().plusDays(1);

        LocalTime time = prevRemindAt.hasTime() ? prevRemindAt.time() : LocalTime.MIDNIGHT;
        LocalDate date = prevRemindAt.hasTime() ? prevRemindAt.date() : prevRemindAt.date().plusDays(1);

        return ZonedDateTime.of(candidateDate, candidateTime, candidate.getZoneId()).isAfter(ZonedDateTime.of(date, time, prevRemindAt.getZoneId()));
    }

    private DateTime getPrevRemindAt(DateTime remindAt, RepeatTime currentRepeatTime, RepeatTime repeatTime) {
        if (repeatTime.isEveryWeeklyTime()) {
            return getEveryWeeklyTimePrevRemindAt(remindAt, repeatTime);
        } else if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
            return getBigIntervalPrevRemindAt(remindAt, currentRepeatTime, repeatTime);
        } else {
            return getShortIntervalPrevRemindAt(remindAt, repeatTime);
        }
    }

    private DateTime getEveryWeeklyTimePrevRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        if (remindAt.date().getDayOfWeek().equals(repeatTime.getDayOfWeek()) && repeatTime.hasTime()) {
            if (remindAt.hasTime()) {
                if (repeatTime.getTime().isBefore(remindAt.time())) {
                    return remindAt.copy().time(repeatTime.getTime());
                }
            } else {
                return remindAt.copy().time(repeatTime.getTime());
            }
        }
        remindAt.copy().date(remindAt.date().with(TemporalAdjusters.previous(repeatTime.getDayOfWeek())));

        return remindAt;
    }

    private DateTime getBigIntervalPrevRemindAt(DateTime remindAt, RepeatTime currentRepeatTime, RepeatTime repeatTime) {
        if (currentRepeatTime.getInterval().equals(repeatTime.getInterval()) && repeatTime.hasTime()) {
            if (remindAt.hasTime()) {
                if (repeatTime.getTime().isBefore(remindAt.time())) {
                    return remindAt.copy().time(repeatTime.getTime());
                }
            } else {
                return remindAt.copy().time(repeatTime.getTime());
            }
        }
        LocalDate nextRemindAtDate = JodaTimeUtils.minus(remindAt.date(), repeatTime.getInterval());

        return DateTime.of(nextRemindAtDate, repeatTime.getTime(), remindAt.getZoneId());
    }

    private DateTime getShortIntervalPrevRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime nextRemindAt = JodaTimeUtils.minus(remindAt.toZonedDateTime(), repeatTime.getInterval());

        return DateTime.of(nextRemindAt);
    }

    private boolean isCanMoveToPrev(ReminderNotification reminderNotification) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow();

        return now.isBefore(reminderNotification.getLastReminderAt());
    }

    private DateTime getNextRemindAt(DateTime remindAt, RepeatTime currentRepeatTime, RepeatTime repeatTime) {
        if (repeatTime.isEveryWeeklyTime()) {
            return getWeeklyNextRemindAt(remindAt, repeatTime);
        } else if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
            return getWeeklyDailyMonthlyYearlyNextRemindAt(remindAt, currentRepeatTime, repeatTime);
        } else {
            return getIntervalNextRemindAt(remindAt, repeatTime);
        }
    }

    private boolean isMoreAppropriateNextRemindAtCandidate(DateTime candidate, DateTime currentNextRemindAt) {
        ZonedDateTime candidateZoned = ZonedDateTime.of(candidate.date(), candidate.hasTime() ? candidate.time() : LocalTime.MIDNIGHT, candidate.getZoneId());
        ZonedDateTime nextRemindAtZoned = ZonedDateTime.of(currentNextRemindAt.date(), currentNextRemindAt.hasTime() ? currentNextRemindAt.time() : LocalTime.MIDNIGHT, currentNextRemindAt.getZoneId());

        return candidateZoned.isBefore(nextRemindAtZoned);
    }

    private DateTime getWeeklyNextRemindAt(DateTime reminderAt, RepeatTime repeatTime) {
        if (reminderAt.date().getDayOfWeek().equals(repeatTime.getDayOfWeek()) && repeatTime.hasTime()) {
            if (reminderAt.hasTime()) {
                if (repeatTime.getTime().isAfter(reminderAt.time())) {
                    return reminderAt.copy().time(repeatTime.getTime());
                }
            } else {
                return reminderAt.copy().time(repeatTime.getTime());
            }
        }
        ZoneId zoneId = reminderAt.getZoneId();
        LocalDate nextDate = reminderAt.date().with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));
        LocalDate now = timeCreator.localDateNow(zoneId);

        while (now.isAfter(nextDate)) {
            nextDate = nextDate.with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));
        }

        return DateTime.of(nextDate, repeatTime.getTime(), zoneId);
    }

    private DateTime getWeeklyDailyMonthlyYearlyNextRemindAt(DateTime remindAt, RepeatTime currentRepeatTime, RepeatTime repeatTime) {
        if (currentRepeatTime.getInterval().equals(repeatTime.getInterval()) && repeatTime.hasTime()) {
            if (remindAt.hasTime()) {
                if (repeatTime.getTime().isAfter(remindAt.time())) {
                    return remindAt.copy().time(repeatTime.getTime());
                }
            } else {
                return remindAt.copy().time(repeatTime.getTime());
            }
        }
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZoneId());
        LocalDate nowDate = now.toLocalDate();
        LocalDate nextRemindAtDate = JodaTimeUtils.plus(remindAt.date(), repeatTime.getInterval());

        while (nowDate.isAfter(nextRemindAtDate)) {
            nextRemindAtDate = JodaTimeUtils.plus(nextRemindAtDate, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAtDate, repeatTime.getTime(), remindAt.getZoneId());
    }

    private DateTime getIntervalNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZoneId());
        ZonedDateTime nextRemindAt = JodaTimeUtils.plus(remindAt.toZonedDateTime(), repeatTime.getInterval());

        while (now.isAfter(nextRemindAt) || now.isEqual(nextRemindAt)) {
            nextRemindAt = JodaTimeUtils.plus(nextRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAt);
    }

    public static class ReminderActionResult {

        private Reminder reminder;

        private ActionResult actionResult;

        private ReminderActionResult(Reminder reminder, ActionResult actionResult) {
            this.reminder = reminder;
            this.actionResult = actionResult;
        }

        public Reminder getReminder() {
            return reminder;
        }

        public ActionResult getActionResult() {
            return actionResult;
        }
    }

    public enum ActionResult {

        NOT_RETURNED,

        RETURNED,

        CURR_SERIES_TO_COMPLETE_CHANGED,

        COMPLETED,

        SKIPPED
    }

    public enum UpdateSeries {

        NONE,

        RESET,

        INCREMENT,

        DECREMENT
    }

    public static class RemindAtCandidate {

        private Integer index;

        private DateTime remindAt;

        private Integer currentSeriesToComplete;

        private RemindAtCandidate(Integer index, DateTime remindAt, Integer currentSeriesToComplete) {
            this.index = index;
            this.remindAt = remindAt;
            this.currentSeriesToComplete = currentSeriesToComplete;
        }

        public Integer getIndex() {
            return index;
        }

        public DateTime getRemindAt() {
            return remindAt;
        }

        public Integer getCurrentSeriesToComplete() {
            return currentSeriesToComplete;
        }
    }
}
