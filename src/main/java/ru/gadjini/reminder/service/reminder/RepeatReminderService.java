package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.CompletedReminderDao;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationAI;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
                new ReminderMapping() {{
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_CHAT_ID));
                    }});
                    setCreatorMapping(new Mapping() {{
                        setFields(Collections.singletonList(CR_CHAT_ID));
                    }});
                    setRemindMessageMapping(new Mapping());
                }}
        );
        completedReminderDao.create(toComplete);
        moveReminderNotificationToNextPeriod(toComplete);

        return toComplete;
    }

    @Transactional
    public Reminder skip(int id) {
        Reminder toSkip = reminderDao.getReminder(
                id,
                new ReminderMapping() {{
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_CHAT_ID));
                    }});
                    setCreatorMapping(new Mapping() {{
                        setFields(Collections.singletonList(CR_CHAT_ID));
                    }});
                    setRemindMessageMapping(new Mapping());
                }}
        );

        moveReminderNotificationToNextPeriod(toSkip);

        return toSkip;
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
            return getWeeklyNextRemindAt(remindAt, repeatTime);
        } else if (repeatTime.getInterval().getDays() > 0) {
            return getDailyNextRemindAt(remindAt, repeatTime);
        } else {
            return getIntervalNextRemindAt(remindAt, repeatTime);
        }
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
                reminderNotificationService.deleteReminderTime(reminderNotification.getId());
            }
        }
        DateTime nextRemindAt = getNextRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAt());
        updateNextRemindAt(id, nextRemindAt);
        reminder.setRemindAt(nextRemindAt);
    }

    private boolean isNotSentYet(Reminder reminder, ReminderNotification reminderNotification) {
        if (reminder.getRepeatRemindAt().getDayOfWeek() != null || reminder.getRepeatRemindAt().getInterval().getDays() != 0) {
            LocalDate remindAt = reminder.getRemindAt().date();

            if (reminder.getRepeatRemindAt().getDayOfWeek() != null) {
                remindAt = remindAt.minusDays(7);
            } else {
                remindAt = remindAt.minusDays(reminder.getRepeatRemindAt().getInterval().getDays());
            }

            if (remindAt.isAfter(reminderNotification.getLastReminderAt().toLocalDate())) {
                return true;
            }
            if (remindAt.isEqual(reminderNotification.getLastReminderAt().toLocalDate())) {
                return true;
            }

            return false;
        } else {
            ZonedDateTime left = reminder.getRemindAt().toZonedDateTime();

            if (left.isAfter(reminderNotification.getLastReminderAt())) {
                return true;
            }
            if (left.isEqual(reminderNotification.getLastReminderAt())) {
                return true;
            }

            return false;
        }
    }

    private DateTime getFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasDayOfWeek()) {
            return getWeeklyFirstRemindAt(repeatTime);
        } else if (repeatTime.getInterval().getDays() > 0) {
            return getDailyFirstRemindAt(repeatTime);
        } else {
            return getIntervalFirstRemindAt(repeatTime);
        }
    }

    private DateTime getIntervalNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime lastRemindAt = remindAt.toZonedDateTime();
        ZonedDateTime now = ZonedDateTime.now(lastRemindAt.getZone());

        if (now.isAfter(lastRemindAt)) {
            while (now.isAfter(lastRemindAt)) {
                lastRemindAt = JodaTimeUtils.plus(lastRemindAt, repeatTime.getInterval());
            }
        } else {
            lastRemindAt = JodaTimeUtils.plus(lastRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(lastRemindAt);
    }

    private DateTime getIntervalFirstRemindAt(RepeatTime repeatTime) {
        ZonedDateTime now = TimeUtils.nowZoned();

        return DateTime.of(now.plusHours(repeatTime.getInterval().getHours()).plusMinutes(repeatTime.getInterval().getMinutes()));
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
        if (repeatTime.hasTime()) {
            ZonedDateTime zonedReminderAt = reminderAt.toZonedDateTime();

            return DateTime.of(zonedReminderAt.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime()));
        }
        LocalDate nextDate = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(reminderAt.date());

        return DateTime.of(nextDate, null, zoneId);
    }

    private DateTime getDailyNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        return remindAt.plusDays(repeatTime.getInterval().getDays());
    }

    private DateTime getDailyFirstRemindAt(RepeatTime repeatTime) {
        if (repeatTime.hasTime()) {
            return DateTime.of(TimeUtils.nowZoned().with(repeatTime.getTime()));
        }

        return DateTime.of(LocalDate.now(), null, ZoneOffset.UTC);
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
        } else if (repeatTime.getInterval().getDays() != 0) {
            addDailyReminderNotificationsWithoutTime(repeatTime, receiverId, reminderNotifications);
        } else {
            addIntervalReminderNotifications(repeatTime, receiverId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithTime(RepeatTime repeatTime, int receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.getDayOfWeek() != null) {
            addWeeklyReminderNotifications(repeatTime, receiverId, reminderNotifications);
        } else if (repeatTime.getInterval().getDays() > 0) {
            addDailyReminderNotifications(repeatTime, receiverId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private void addIntervalReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = JodaTimeUtils.plus(TimeUtils.nowZoned(), repeatTime.getInterval());

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

    private void addDailyReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = TimeUtils.nowZoned();
        ZonedDateTime repeatReminder = now.with(repeatTime.getTime());

        fixedReminderNotification(repeatReminder.toLocalDate(), repeatTime.getInterval().getDays(), repeatTime.getTime(), reminderNotifications).setItsTime(true);
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (repeatTime.getInterval().getDays() == 1 && offsetTime.getDays() != 0) {
                continue;
            }
            ReminderNotification reminderNotification = fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    repeatTime.getInterval().getDays(),
                    offsetTime.getTime() == null ? repeatTime.getTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            );
            reminderNotification.setCustom(true);
        }
    }

    private void addWeeklyReminderNotifications(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = TimeUtils.nowZoned();
        ZonedDateTime repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek())).with(repeatTime.getTime());

        fixedReminderNotification(repeatReminder.toLocalDate(), 7, repeatTime.getTime(), reminderNotifications).setItsTime(true);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    7,
                    offsetTime.getTime() == null ? repeatTime.getTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime(),
                    reminderNotifications
            ).setCustom(true);
        }
    }

    private void addWeeklyReminderNotificationsWithoutTime(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        LocalDate now = LocalDate.now();
        LocalDate repeatReminder = now.with(TemporalAdjusters.next(repeatTime.getDayOfWeek()));

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), 7, offsetTime.getTime(), reminderNotifications).setCustom(true);
        }
    }

    private void addDailyReminderNotificationsWithoutTime(RepeatTime repeatTime, int receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = ZonedDateTime.now();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (repeatTime.getInterval().getDays() == 1 && offsetTime.getDays() != 0) {
                continue;
            }

            ReminderNotification notification = fixedReminderNotification(
                    now.minusDays(offsetTime.getDays()).toLocalDate(),
                    repeatTime.getInterval().getDays(),
                    offsetTime.getTime(),
                    reminderNotifications
            );
            notification.setCustom(true);
        }
    }

    private ReminderNotification fixedReminderNotification(LocalDate repeatAt, int repeatDays, LocalTime localTime, List<ReminderNotification> reminderNotifications) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        ZonedDateTime lastRemindAt = ZonedDateTime.of(repeatAt, localTime, ZoneOffset.UTC);
        if (lastRemindAt.isAfter(ZonedDateTime.now())) {
            lastRemindAt.minusDays(repeatDays);
        }
        reminderNotification.setLastReminderAt(lastRemindAt);
        reminderNotification.setDelayTime(new Period().withDays(repeatDays));
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }
}
