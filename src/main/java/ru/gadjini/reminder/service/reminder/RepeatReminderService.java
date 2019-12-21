package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.dao.CompletedReminderDao;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.*;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.Mapping;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
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

    public List<Reminder> getOverdueRepeatReminders() {
        return reminderDao.getOverdueRepeatReminders();
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

    @Transactional
    public Reminder changeReminderTime(int reminderId, int receiverId, RepeatTime repeatTime) {
        DateTime nextRemindAt = getFirstRemindAt(repeatTime);
        PGobject sqlObject = nextRemindAt.sqlObject();
        Reminder reminder = reminderDao.update(
                new HashMap<>() {{
                    put(ReminderTable.TABLE.REPEAT_REMIND_AT, repeatTime.sqlObject());
                    put(ReminderTable.TABLE.INITIAL_REMIND_AT, sqlObject);
                    put(ReminderTable.TABLE.REMIND_AT, sqlObject);
                }},
                ReminderTable.TABLE.ID.eq(reminderId),
                new ReminderMapping() {{
                    setRemindMessageMapping(new Mapping());
                    setReceiverMapping(new Mapping() {{
                        setFields(List.of(ReminderMapping.RC_CHAT_ID));
                    }});
                }}
        );

        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> reminderNotifications = getRepeatReminderNotifications(repeatTime, receiverId);
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);

        reminder.setCreator(TgUser.from(securityService.getAuthenticatedUser()));

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
        if (reminder.getRepeatRemindAt().getDayOfWeek() != null || reminder.getRepeatRemindAt().getInterval().getDays() != 0) {
            return isNotSentYetForDailyTime(reminder, reminderNotification);
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

    private boolean isNotSentYetForDailyTime(Reminder reminder, ReminderNotification reminderNotification) {
        LocalDate remindAt = reminder.getRemindAt().date();

        if (reminder.getRepeatRemindAt().getDayOfWeek() != null) {
            remindAt = remindAt.minusDays(7);
        } else {
            remindAt = remindAt.minusDays(reminder.getRepeatRemindAt().getInterval().getDays());
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
        } else {
            return getIntervalFirstRemindAt(repeatTime);
        }
    }

    private DateTime getIntervalNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        ZonedDateTime nextRemindAt = JodaTimeUtils.plus(remindAt.toZonedDateTime(), repeatTime.getInterval());
        ZonedDateTime now = ZonedDateTime.now(nextRemindAt.getZone());

        while (now.isAfter(nextRemindAt)) {
            nextRemindAt = JodaTimeUtils.plus(nextRemindAt, repeatTime.getInterval());
        }

        return DateTime.of(nextRemindAt);
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
        LocalDate nextDate = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(reminderAt.date());
        LocalDate now = LocalDate.now();

        while (now.isAfter(nextDate)) {
            nextDate = (LocalDate) TemporalAdjusters.next(repeatTime.getDayOfWeek()).adjustInto(nextDate);
        }

        return DateTime.of(nextDate, repeatTime.getTime(), zoneId);
    }

    private DateTime getDailyNextRemindAt(DateTime remindAt, RepeatTime repeatTime) {
        LocalDate localDate = remindAt.date().plusDays(repeatTime.getInterval().getDays());
        LocalDate now = LocalDate.now();

        while (now.isAfter(localDate)) {
            localDate = localDate.plusDays(repeatTime.getInterval().getDays());
        }

        return DateTime.of(localDate, remindAt.time(), remindAt.getZone());
    }

    private DateTime getDailyFirstRemindAt(RepeatTime repeatTime) {
        return DateTime.of(LocalDate.now(), repeatTime.getTime(), ZoneOffset.UTC);
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
            lastRemindAt = lastRemindAt.minusDays(repeatDays);
        }
        reminderNotification.setLastReminderAt(lastRemindAt);
        reminderNotification.setDelayTime(new Period().withDays(repeatDays));
        reminderNotifications.add(reminderNotification);

        return reminderNotification;
    }
}
