package ru.gadjini.reminder.service.reminder.notification;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.*;

@Service
public class ReminderNotificationAI {

    private static final int MINUTE_DIFF = 20;

    private static final int ITS_TIME_MINUTE_DIFF = 5;

    private TimeCreator timeCreator;

    @Autowired
    public ReminderNotificationAI(TimeCreator timeCreator) {
        this.timeCreator = timeCreator;
    }

    public boolean isNeedCreateItsTimeNotification(ZonedDateTime remindAt) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZone());

        return now.plusMinutes(ITS_TIME_MINUTE_DIFF).isBefore(remindAt);
    }

    public boolean isNeedCreateReminderNotification(Period period, UserReminderNotification offsetTime) {
        return period.toStandardMinutes().getMinutes() > offsetTime.getHours() * 60 + offsetTime.getMinutes() + MINUTE_DIFF;
    }

    public boolean isNeedCreateReminderNotification(ZonedDateTime remindAt, UserReminderNotification offsetTime) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZone());

        if (offsetTime.getTime() == null) {
            return Duration.between(now, remindAt).toMinutes() > offsetTime.getHours() * 60 + offsetTime.getMinutes() + MINUTE_DIFF;
        } else {
            return remindAt.getDayOfMonth() - now.getDayOfMonth() > 1 || remindAt.getDayOfMonth() - now.getDayOfMonth() == 1 && now.toLocalTime().plusMinutes(MINUTE_DIFF).isBefore(offsetTime.getTime());
        }
    }

    public boolean isNeedCreateReminderNotification(LocalDate remindAt, UserReminderNotification offsetTime) {
        return isNeedCreateReminderNotification(remindAt, offsetTime.getDays(), offsetTime.getTime());
    }

    public boolean isNeedCreateReminderNotification(LocalDate remindAt, int days, LocalTime time) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(remindAt.minusDays(days), time, ZoneOffset.UTC);

        return now.isBefore(zonedDateTime);
    }
}
