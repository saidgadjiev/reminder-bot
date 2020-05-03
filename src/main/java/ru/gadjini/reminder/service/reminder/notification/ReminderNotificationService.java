package ru.gadjini.reminder.service.reminder.notification;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.ReminderNotificationDao;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.jooq.ReminderNotificationTable;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.DateTimeService;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ReminderNotificationService {

    private ReminderNotificationTable reminderNotificationTable = ReminderNotificationTable.TABLE.as("rt");

    private ReminderNotificationDao reminderNotificationDao;

    private DateTimeService timeCreator;

    @Autowired
    public ReminderNotificationService(ReminderNotificationDao reminderNotificationDao, DateTimeService timeCreator) {
        this.reminderNotificationDao = reminderNotificationDao;
        this.timeCreator = timeCreator;
    }

    public void create(ReminderNotification reminderNotification) {
        reminderNotificationDao.create(reminderNotification);
    }

    public void create(List<ReminderNotification> reminderNotifications) {
        reminderNotificationDao.create(reminderNotifications);
    }

    public void deleteReminderNotifications(int reminderId) {
        reminderNotificationDao.delete(ReminderNotificationTable.TABLE.REMINDER_ID.eq(reminderId));
    }

    public void deleteCustomReminderNotifications(int reminderId) {
        reminderNotificationDao.delete(ReminderNotificationTable.TABLE.REMINDER_ID.eq(reminderId).and(ReminderNotificationTable.TABLE.CUSTOM.eq(true)));
    }

    public int deleteReminderNotification(int id) {
        return reminderNotificationDao.delete(id);
    }

    public void updateLastRemindAt(int id, LocalDateTime lastReminderAt) {
        reminderNotificationDao.updateLastRemindAt(id, lastReminderAt);
    }

    public List<ReminderNotification> getCustomRemindersList(int reminderId) {
        return reminderNotificationDao.getList(reminderNotificationTable.REMINDER_ID.equal(reminderId).and(reminderNotificationTable.CUSTOM.equal(Boolean.TRUE)));
    }

    public List<ReminderNotification> getList(int reminderId) {
        return reminderNotificationDao.getList(reminderNotificationTable.REMINDER_ID.equal(reminderId));
    }

    public ReminderNotification getReminderTime(int id) {
        return reminderNotificationDao.getById(id);
    }

    public ReminderNotification createReminderNotification(RepeatTime repeatTime) {
        if (repeatTime.hasDayOfWeek()) {
            return getEveryWeekly(repeatTime);
        } else if (TimeUtils.isBigInterval(repeatTime.getInterval())) {
            return getBigInterval(repeatTime);
        } else {
            ZonedDateTime now = timeCreator.zonedDateTimeNow(repeatTime.getZoneId());
            return intervalReminderTime(now, repeatTime.getInterval());
        }
    }

    public ReminderNotification intervalReminderTime(ZonedDateTime remindAt, Period interval) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(remindAt);
        reminderNotification.setDelayTime(interval);

        return reminderNotification;
    }

    public ReminderNotification intervalReminderNotification(ZonedDateTime lastRemindAt, Period interval) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        if (lastRemindAt.isAfter(timeCreator.zonedDateTimeNow())) {
            lastRemindAt = JodaTimeUtils.minus(lastRemindAt, interval);
        }
        reminderNotification.setLastReminderAt(lastRemindAt);
        reminderNotification.setDelayTime(interval);

        return reminderNotification;
    }

    public ReminderNotification fixedReminderNotification(LocalDate repeatAt, Period period, LocalTime localTime) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        ZonedDateTime lastRemindAt = ZonedDateTime.of(repeatAt, localTime, ZoneOffset.UTC);
        if (lastRemindAt.isAfter(timeCreator.zonedDateTimeNow())) {
            lastRemindAt = JodaTimeUtils.minus(lastRemindAt, period);
        }
        reminderNotification.setLastReminderAt(lastRemindAt);
        reminderNotification.setDelayTime(period);

        return reminderNotification;
    }

    private ReminderNotification intervalReminderTime(LocalDate repeatAt, Period period, LocalTime localTime) {
        ReminderNotification reminderNotification = ReminderNotification.repeatTime();
        reminderNotification.setLastReminderAt(ZonedDateTime.of(JodaTimeUtils.minus(repeatAt, period), localTime, ZoneOffset.UTC));
        reminderNotification.setDelayTime(period);

        return reminderNotification;
    }

    private ReminderNotification getBigInterval(RepeatTime repeatTime) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(repeatTime.getZoneId());
        ZonedDateTime repeatAt = now.with(repeatTime.getTime());

        if (repeatAt.isBefore(now)) {
            repeatAt = JodaTimeUtils.plus(repeatAt, repeatTime.getInterval());
        }

        return intervalReminderTime(repeatAt.toLocalDate(), repeatTime.getInterval(), repeatTime.getTime());
    }

    private ReminderNotification getEveryWeekly(RepeatTime repeatTime) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(repeatTime.getZoneId());
        ZonedDateTime repeatReminder = now.with(TemporalAdjusters.nextOrSame(repeatTime.getDayOfWeek())).with(repeatTime.getTime());

        return intervalReminderTime(repeatReminder.toLocalDate(), repeatTime.getInterval(), repeatTime.getTime());
    }
}
