package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
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
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RepeatReminderService {

    private ReminderDao reminderDao;

    private CompletedReminderDao completedReminderDao;

    private ReminderNotificationService reminderNotificationService;

    private ReminderNotificationAI reminderNotificationAI;

    private UserReminderNotificationService userReminderNotificationService;

    private SecurityService securityService;

    @Autowired
    public RepeatReminderService(ReminderDao reminderDao, CompletedReminderDao completedReminderDao,
                                 ReminderNotificationService reminderNotificationService,
                                 ReminderNotificationAI reminderNotificationAI,
                                 UserReminderNotificationService userReminderNotificationService,
                                 SecurityService securityService) {
        this.reminderDao = reminderDao;
        this.completedReminderDao = completedReminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderNotificationAI = reminderNotificationAI;
        this.userReminderNotificationService = userReminderNotificationService;
        this.securityService = securityService;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        DateTime nextRemindAt = getFirstRemindAt(reminder.getRepeatRemindAt());
        reminder.setRemindAt(nextRemindAt);

        Reminder created = reminderDao.create(reminder);
        List<ReminderNotification> reminderNotifications = getRepeatReminderNotifications(reminder.getRepeatRemindAt(), reminder.getReceiverId());
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(created.getId()));
        reminderNotificationService.create(reminderNotifications);

        return created;
    }

    @Transactional
    public Reminder complete(int id) {
        Reminder toComplete = reminderDao.getReminder(
                id,
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_CHAT_ID)))
                        .setCreatorMapping(new Mapping().setFields(List.of(ReminderMapping.CR_CHAT_ID)))
                        .setRemindMessageMapping(new Mapping())
        );
        completedReminderDao.create(toComplete);
        moveReminderNotificationToNextPeriod(toComplete);
        toComplete.getReceiver().setFrom(securityService.getAuthenticatedUser());

        return toComplete;
    }

    public List<Reminder> getOverdueRepeatReminders() {
        return reminderDao.getOverdueRepeatReminders();
    }

    @Transactional
    public Reminder skip(int id) {
        Reminder toSkip = reminderDao.getReminder(
                id,
                new ReminderMapping()
                        .setReceiverMapping(new Mapping().setFields(List.of(ReminderMapping.RC_CHAT_ID)))
                        .setCreatorMapping(new Mapping().setFields(List.of(ReminderMapping.CR_CHAT_ID)))
                        .setRemindMessageMapping(new Mapping())
        );
        toSkip.getReceiver().setFrom(securityService.getAuthenticatedUser());
        moveReminderNotificationToNextPeriod(toSkip);

        return toSkip;
    }

    public void updateNextRemindAt(int reminderId, DateTime nextRemindAt) {
        reminderDao.update(
                Map.ofEntries(
                        Map.entry(ReminderTable.TABLE.REMIND_AT, nextRemindAt.sqlObject()),
                        Map.entry(ReminderTable.TABLE.INITIAL_REMIND_AT, nextRemindAt.sqlObject())
                ),
                ReminderTable.TABLE.ID.equal(reminderId),
                null
        );
    }

    public DateTime getNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        if (repeatTime.getDayOfWeek() != null) {
            return getWeeklyNextRemindAt(remindAt, repeatTime);
        } else {
            return getIntervalNextRemindAt(remindAt, repeatTime);
        }
    }

    @Transactional
    public Reminder changeReminderTime(int reminderId, int receiverId, RepeatTime repeatTime) {
        DateTime nextRemindAt = getFirstRemindAt(repeatTime);
        PGobject sqlObject = nextRemindAt.sqlObject();
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
        List<ReminderNotification> reminderNotifications = getRepeatReminderNotifications(repeatTime, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);

        Reminder reminder = new Reminder();
        reminder.setRepeatRemindAt(repeatTime);
        reminder.setInitialRemindAt(nextRemindAt);
        reminder.setRemindAt(nextRemindAt);

        return reminder;
    }

    private void moveReminderNotificationToNextPeriod(Reminder reminder) {
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
        DateTime nextRemindAt = getNextRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAt());
        updateNextRemindAt(id, nextRemindAt);
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

    private DateTime getFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasDayOfWeek()) {
            return getWeeklyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getDays() > 0) {
            return getDailyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getMonths() != 0) {
            return getMonthlyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getYears() != 0) {
            return getYearlyFirstRemindAt(repeatTime);
        } else {
            return getIntervalFirstRemindAt(repeatTime);
        }
    }

    private DateTime getIntervalFirstRemindAt(RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.nowZoned();

        return DateTime.of(JodaTimeUtils.plus(now, repeatTime.getInterval()));
    }

    private DateTime getWeeklyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = TimeUtils.nowZoned().with(repeatTime.getTime());

            if (!now.getDayOfWeek().equals(repeatTime.getDayOfWeek())) {
                now = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));
            }

            return DateTime.of(now);
        }
        LocalDate now = LocalDate.now();

        if (now.getDayOfWeek().equals(repeatTime.getDayOfWeek())) {
            DateTime.of(now, null, ZoneOffset.UTC);
        }
        now = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(now);

        return DateTime.of(now, null, ZoneOffset.UTC);
    }

    private DateTime getWeeklyNextRemindAt(DateTime reminderAt, RepeatTime repeatTime) {
        ZoneId zoneId = reminderAt.getZone();
        LocalDate nextDate = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(reminderAt.date());
        LocalDate now = LocalDate.now();

        while (now.isAfter(nextDate)) {
            nextDate = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(nextDate);
        }

        return DateTime.of(nextDate, repeatTime.getTime(), zoneId);
    }

    private DateTime getDailyFirstRemindAt(RepeatTime repeatTime) {
        return DateTime.of(LocalDate.now(), repeatTime.getTime(), ZoneOffset.UTC);
    }

    private DateTime getIntervalNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        ZonedDateTime nextRemindAt = JodaTimeUtils.plus(remindAt.toZonedDateTime(), repeatTime.getInterval());

        while (now.isAfter(nextRemindAt)) {
            now = JodaTimeUtils.plus(nextRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAt);
    }

    private DateTime getMonthlyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            ZonedDateTime now = ZonedDateTime.now(repeatTime.getZoneId());
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
            ZonedDateTime now = ZonedDateTime.now(repeatTime.getZoneId());
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
        ZonedDateTime now = getFirstRemindAt(repeatTime).toZonedDateTime();

        intervalReminderNotification(now, repeatTime.getInterval(), reminderNotifications).setItsTime(true);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (offsetTime.getTime() == null && reminderNotificationAI.isNeedCreateReminderNotification(repeatTime.getInterval(), offsetTime)) {
                intervalReminderNotification(now.minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()), repeatTime.getInterval(), reminderNotifications).setCustom(true);
            }
        }
    }

    private ReminderNotification intervalReminderNotification(ZonedDateTime remindAt, Period interval, List<ReminderNotification> reminderNotifications) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(remindAt);
        reminderNotification.setDelayTime(interval);
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }

    private void addYearlyOrMonthlyOrDailyReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime repeatReminder = getFirstRemindAt(repeatTime).toZonedDateTime();

        fixedReminderNotification(repeatReminder.toLocalDate(), repeatTime.getInterval(), repeatTime.getTime(), reminderNotifications).setItsTime(true);
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (repeatTime.getInterval().getDays() == 1 && offsetTime.getDays() != 0) {
                continue;
            }
            ReminderNotification reminderNotification = fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    repeatTime.getInterval(),
                    offsetTime.getTime() == null ? repeatTime.getTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            );
            reminderNotification.setCustom(true);
        }
    }

    private void addWeeklyReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime repeatReminder = getFirstRemindAt(repeatTime).toZonedDateTime();

        Period repeatPeriod = new Period().withDays(7);
        fixedReminderNotification(repeatReminder.toLocalDate(), repeatPeriod, repeatTime.getTime(), reminderNotifications).setItsTime(true);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    repeatPeriod,
                    offsetTime.getTime() == null ? repeatTime.getTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            ).setCustom(true);
        }
    }

    private void addMonthlyOrYearlyOrDailyReminderNotificationsWithoutTime(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        LocalDate repeatReminder = getFirstRemindAt(repeatTime).date();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (repeatTime.getInterval().getDays() == 1 && offsetTime.getDays() != 0) {
                continue;
            }
            fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), repeatTime.getInterval(), offsetTime.getTime(), reminderNotifications).setCustom(true);
        }
    }

    private void addWeeklyReminderNotificationsWithoutTime(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        LocalDate repeatReminder = getFirstRemindAt(repeatTime).date();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        Period repeatPeriod = new Period().withDays(7);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), repeatPeriod, offsetTime.getTime(), reminderNotifications).setCustom(true);
        }
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
}
