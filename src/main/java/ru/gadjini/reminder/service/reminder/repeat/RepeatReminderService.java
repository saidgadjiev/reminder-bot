package ru.gadjini.reminder.service.reminder.repeat;

import org.joda.time.Period;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.RemindAtCandidate;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RepeatReminderService {

    private ReminderDao reminderDao;

    private ReminderNotificationService reminderNotificationService;

    private ReminderNotificationAI reminderNotificationAI;

    private UserReminderNotificationService userReminderNotificationService;

    private DateTimeService timeCreator;

    private RepeatReminderBusinessService repeatReminderBusinessService;

    @Autowired
    public RepeatReminderService(ReminderDao reminderDao, ReminderNotificationService reminderNotificationService,
                                 ReminderNotificationAI reminderNotificationAI,
                                 UserReminderNotificationService userReminderNotificationService,
                                 DateTimeService timeCreator, RepeatReminderBusinessService repeatReminderBusinessService) {
        this.reminderDao = reminderDao;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderNotificationAI = reminderNotificationAI;
        this.userReminderNotificationService = userReminderNotificationService;
        this.timeCreator = timeCreator;
        this.repeatReminderBusinessService = repeatReminderBusinessService;
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
    public Reminder changeReminderTime(int reminderId, long receiverId, List<RepeatTime> repeatTimesInReceiverZone) {
        RemindAtCandidate remindAtCandidate = repeatReminderBusinessService.getFirstRemindAt(repeatTimesInReceiverZone);
        DateTime firstRemindAt = remindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC);
        PGobject sqlObject = firstRemindAt.sqlObject();
        List<RepeatTime> repeatTimes = timeCreator.withZone(repeatTimesInReceiverZone, ZoneOffset.UTC);
        reminderDao.update(
                Map.ofEntries(
                        Map.entry(ReminderTable.TABLE.REPEAT_REMIND_AT, repeatTimes.stream().map(RepeatTimeRecord::new).toArray()),
                        Map.entry(ReminderTable.TABLE.INITIAL_REMIND_AT, sqlObject),
                        Map.entry(ReminderTable.TABLE.REMIND_AT, sqlObject),
                        Map.entry(ReminderTable.TABLE.CURR_REPEAT_INDEX, remindAtCandidate.getIndex())
                ),
                ReminderTable.TABLE.ID.eq(reminderId),
                null
        );

        List<ReminderNotification> notifications = updateReminderNotifications(reminderId, receiverId, repeatTimesInReceiverZone);
        Reminder reminder = new Reminder();
        reminder.setRepeatRemindAts(repeatTimes);
        reminder.setInitialRemindAt(firstRemindAt);
        reminder.setRemindAt(firstRemindAt);
        reminder.setCurrRepeatIndex(remindAtCandidate.getIndex());
        reminder.setSuppressNotifications(notifications.size() == 0);

        return reminder;
    }

    public List<Reminder> getOverdueRepeatReminders() {
        return reminderDao.getOverdueRepeatReminders();
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

    public List<ReminderNotification> updateReminderNotifications(int reminderId, long receiverId, List<RepeatTime> repeatTimesInReceiverZone) {
        reminderNotificationService.deleteReminderNotifications(reminderId);
        List<ReminderNotification> reminderNotifications = new ArrayList<>();
        for (RepeatTime repeatTimeInReceiverZone : repeatTimesInReceiverZone) {
            reminderNotifications.addAll(getRepeatReminderNotifications(repeatTimeInReceiverZone, receiverId));
        }
        reminderNotifications.forEach(reminderNotification -> reminderNotification.setReminderId(reminderId));
        reminderNotificationService.create(reminderNotifications);

        return reminderNotifications;
    }

    private List<ReminderNotification> getRepeatReminderNotifications(RepeatTime repeatTime, long receiverId) {
        if (!repeatTime.hasTime()) {
            return getRepeatReminderNotificationsWithoutTime(repeatTime, receiverId);
        }

        return getRepeatReminderNotificationsWithTime(repeatTime, receiverId);
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithoutTime(RepeatTime repeatTime, long receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.isEveryWeeklyTime()) {
            addWeeklyReminderNotificationsWithoutTime(repeatTime, receiverId, reminderNotifications);
        } else if (repeatTime.getInterval() != null) {
            if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
                addWeeklyMonthlyOrYearlyOrDailyReminderNotificationsWithoutTime(repeatTime, receiverId, reminderNotifications);
            } else {
                addIntervalReminderNotifications(repeatTime, receiverId, reminderNotifications);
            }
        }

        return reminderNotifications;
    }

    private List<ReminderNotification> getRepeatReminderNotificationsWithTime(RepeatTime repeatTime, long receiverId) {
        List<ReminderNotification> reminderNotifications = new ArrayList<>();

        if (repeatTime.isEveryWeeklyTime()) {
            addWeeklyReminderNotifications(repeatTime, receiverId, reminderNotifications);
        } else if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
            addYearlyOrMonthlyOrDailyReminderNotifications(repeatTime, receiverId, reminderNotifications);
        }

        return reminderNotifications;
    }

    private void addIntervalReminderNotifications(RepeatTime repeatTime, long receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime now = repeatReminderBusinessService.getFirstRemindAt(repeatTime).withZoneSameInstant(ZoneOffset.UTC).toZonedDateTime();

        ReminderNotification notification = reminderNotificationService.intervalReminderNotification(now, repeatTime.getInterval());
        notification.setItsTime(true);
        reminderNotifications.add(notification);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (offsetTime.getTime() == null && reminderNotificationAI.isNeedCreateReminderNotification(repeatTime.getInterval(), offsetTime)) {
                ReminderNotification reminderNotification = reminderNotificationService.intervalReminderNotification(now.minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()), repeatTime.getInterval());
                reminderNotification.setCustom(true);
                reminderNotifications.add(reminderNotification);
            }
        }
    }

    private void addYearlyOrMonthlyOrDailyReminderNotifications(RepeatTime repeatTime, long receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime repeatReminder = repeatReminderBusinessService.getFirstRemindAt(repeatTime).withZoneSameInstant(ZoneOffset.UTC).toZonedDateTime();

        ReminderNotification notification = reminderNotificationService.fixedReminderNotification(repeatReminder.toLocalDate(), repeatTime.getInterval(), repeatReminder.toLocalTime());
        notification.setItsTime(true);
        reminderNotifications.add(notification);
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);

        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (repeatTime.getInterval().getDays() == 1 && userReminderNotification.getDays() != 0) {
                continue;
            }
            ReminderNotification reminderNotification = reminderNotificationService.fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(userReminderNotification.getDays()),
                    repeatTime.getInterval(),
                    userReminderNotification.getTime() == null ? repeatReminder.toLocalTime().minusHours(userReminderNotification.getHours()).minusMinutes(userReminderNotification.getMinutes()) : userReminderNotification.getTime()
            );
            reminderNotification.setCustom(true);
            reminderNotifications.add(reminderNotification);
        }
    }

    private void addWeeklyReminderNotifications(RepeatTime repeatTime, long receiverId, List<ReminderNotification> reminderNotifications) {
        ZonedDateTime repeatReminder = repeatReminderBusinessService.getFirstRemindAt(repeatTime).withZoneSameInstant(ZoneOffset.UTC).toZonedDateTime();

        Period repeatPeriod = new Period().withWeeks(1);
        ReminderNotification notification = reminderNotificationService.fixedReminderNotification(repeatReminder.toLocalDate(), repeatPeriod, repeatReminder.toLocalTime());
        notification.setItsTime(true);
        reminderNotifications.add(notification);

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITH_TIME);
        for (UserReminderNotification offsetTime : userReminderNotifications) {
            ReminderNotification reminderNotification = reminderNotificationService.fixedReminderNotification(
                    repeatReminder.toLocalDate().minusDays(offsetTime.getDays()),
                    repeatPeriod,
                    offsetTime.getTime() == null ? repeatReminder.toLocalTime().minusHours(offsetTime.getHours()).minusMinutes(offsetTime.getMinutes()) : offsetTime.getTime()
            );
            reminderNotification.setCustom(true);
            reminderNotifications.add(reminderNotification);
        }
    }

    private void addWeeklyMonthlyOrYearlyOrDailyReminderNotificationsWithoutTime(RepeatTime repeatTimeInReceiverZone, long receiverId, List<ReminderNotification> reminderNotifications) {
        LocalDate repeatReminder = repeatReminderBusinessService.getFirstRemindAt(repeatTimeInReceiverZone).date();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            if (repeatTimeInReceiverZone.getInterval().getDays() == 1 && offsetTime.getDays() != 0) {
                continue;
            }
            ReminderNotification reminderNotification = reminderNotificationService.fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), repeatTimeInReceiverZone.getInterval(), offsetTime.getTime());
            reminderNotification.setCustom(true);
            reminderNotifications.add(reminderNotification);
        }
    }

    private void addWeeklyReminderNotificationsWithoutTime(RepeatTime repeatTimeInReceiverZone, long receiverId, List<ReminderNotification> reminderNotifications) {
        LocalDate repeatReminder = repeatReminderBusinessService.getFirstRemindAt(repeatTimeInReceiverZone).date();

        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(receiverId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        Period repeatPeriod = new Period().withWeeks(1);

        for (UserReminderNotification offsetTime : userReminderNotifications) {
            ReminderNotification reminderNotification = reminderNotificationService.fixedReminderNotification(repeatReminder.minusDays(offsetTime.getDays()), repeatPeriod, offsetTime.getTime());
            reminderNotification.setCustom(true);
            reminderNotifications.add(reminderNotification);
        }
    }
}
