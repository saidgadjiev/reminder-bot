package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.service.reminder.notification.ReminderTimeAI;
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RepeatReminderService {

    private ReminderDao reminderDao;

    private ReminderNotificationService reminderNotificationService;

    private TgUserService userService;

    private ReminderTimeAI reminderTimeAI;

    private SecurityService securityService;

    private UserReminderNotificationService userReminderNotificationService;

    @Autowired
    public RepeatReminderService(ReminderDao reminderDao, ReminderNotificationService reminderNotificationService,
                                 TgUserService userService, ReminderTimeAI reminderTimeAI, SecurityService securityService) {
        this.reminderDao = reminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.userService = userService;
        this.reminderTimeAI = reminderTimeAI;
        this.securityService = securityService;
    }

    @Transactional
    public Reminder createReminder(Reminder reminder) {
        ZoneId zoneId = userService.getTimeZone(reminder.getReceiverId());
        DateTime nextRemindAtInReceiverZone = getFirstRemindAt(zoneId, reminder.getRepeatRemindAt());
        reminder.setRemindAt(nextRemindAtInReceiverZone.withZoneSameInstant(ZoneOffset.UTC));

        Reminder created = reminderDao.create(reminder);
        List<ReminderNotification> reminderNotifications = getRepeatReminderNotifications(reminder.getRepeatRemindAt(), zoneId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(created.getId()));
        reminderNotificationService.create(reminderNotifications);

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

    private List<ReminderNotification> getRepeatReminderNotifications(RepeatTime repeatTime, ZoneId zoneId) {
        if (!repeatTime.hasTime()) {
            return getRepeatReminderNotificationsWithoutTime(repeatTime, zoneId);
        }

        return getRepeatReminderNotificationsWithTime(repeatTime, zoneId);
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithoutTime(RepeatTime repeatTime, ZoneId zoneId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.hasDayOfWeek()) {
            addWeeklyReminderNotificationsWithoutTime(repeatTime, zoneId, reminderNotifications);
        } else {
            addDailyReminderNotificationsWithoutTime(repeatTime, zoneId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithTime(RepeatTime repeatTime, ZoneId zoneId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.getDayOfWeek() != null) {
            addWeeklyReminderNotifications(repeatTime, zoneId, reminderNotifications);
        } else if (repeatTime.getInterval().getDays() > 0) {
            addDailyReminderNotifications(repeatTime, zoneId, reminderNotifications);
        } else {
            addIntervalReminderNotifications(repeatTime, zoneId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private void addIntervalReminderNotifications(RepeatTime repeatTime, ZoneId zoneId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = TimeUtils.now(zoneId);

        intervalReminderNotification(now, repeatTime.getInterval(), reminderNotifications).setItsTime(true);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(securityService.getAuthenticatedUser().getId(), UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (offsetTime.getTime() == null && reminderTimeAI.isNeedCreateReminderNotification(repeatTime.getInterval(), offsetTime)) {
                intervalReminderNotification(now.minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()), repeatTime.getInterval(), reminderNotifications).setCustom(true);
            }
        }
    }

    private ReminderNotification intervalReminderNotification(ZonedDateTime remindAt, Period interval, List<ReminderNotification> reminderNotifications) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(remindAt.withZoneSameInstant(ZoneOffset.UTC));
        reminderNotification.setDelayTime(interval);
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }

    private void addDailyReminderNotifications(RepeatTime repeatTime, ZoneId zoneId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = TimeUtils.now(zoneId);
        ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

        if (repeatReminder.isBefore(now)) {
            repeatReminder.plusDays(repeatTime.getInterval().getDays());
        }
        fixedReminderNotification(repeatReminder.toLocalDate(), zoneId, repeatTime.getInterval().getDays(), repeatTime.getTime(), reminderNotifications).setItsTime(true);
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(securityService.getAuthenticatedUser().getId(), UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    zoneId,
                    repeatTime.getInterval().getDays(),
                    offsetTime.getTime() == null ? repeatTime.getTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            ).setCustom(true);
        }
    }

    private void addWeeklyReminderNotifications(RepeatTime repeatTime, ZoneId zoneId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = TimeUtils.now(zoneId);
        ZonedDateTime repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());

        fixedReminderNotification(repeatReminder.toLocalDate(), zoneId, 7, repeatTime.getTime(), reminderNotifications).setItsTime(true);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(securityService.getAuthenticatedUser().getId(), UserReminderNotification.NotificationType.WITH_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    zoneId,
                    7,
                    offsetTime.getTime() == null ? repeatTime.getTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            ).setCustom(true);
        }
    }

    private void addWeeklyReminderNotificationsWithoutTime(RepeatTime repeatTime, ZoneId zoneId, List<ReminderNotification> reminderNotifications) {
        LocalDate now = LocalDate.now(zoneId);
        LocalDate repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(securityService.getAuthenticatedUser().getId(), UserReminderNotification.NotificationType.WITHOUT_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), zoneId, 7, offsetTime.getTime(), reminderNotifications).setCustom(true);
        }
    }

    private void addDailyReminderNotificationsWithoutTime(RepeatTime repeatTime, ZoneId zoneId, List<ReminderNotification> reminderNotifications) {
        LocalDate repeatReminder = LocalDate.now(zoneId);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(securityService.getAuthenticatedUser().getId(), UserReminderNotification.NotificationType.WITHOUT_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (offsetTime.getDays() == 0) {
                fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), zoneId, repeatTime.getInterval().getDays(), offsetTime.getTime(), reminderNotifications).setCustom(true);
            }
        }
    }

    private ReminderNotification fixedReminderNotification(LocalDate repeatAt, ZoneId zoneId, int repeatDays, LocalTime localTime, List<ReminderNotification> reminderNotifications) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(ZonedDateTime.of(repeatAt.minusDays(repeatDays), localTime, zoneId).withZoneSameInstant(ZoneOffset.UTC));
        reminderNotification.setDelayTime(new Period().withDays(repeatDays));
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }
}
