package ru.gadjini.reminder.service.reminder.notification;

import org.joda.time.Period;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.UserReminderNotification;

import java.time.*;

@Service
public class ReminderTimeAI {

    private static final int MINUTE_DIFF = 20;

    public boolean isNeedCreateReminderNotification(Period period, UserReminderNotification.OffsetTime offsetTime) {
        return period.toStandardMinutes().getMinutes() >= offsetTime.getHour() * 60 + offsetTime.getMinute() + MINUTE_DIFF;
    }

    public boolean isNeedCreateReminderNotification(ZonedDateTime remindAt, UserReminderNotification.OffsetTime offsetTime) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (offsetTime.getLocalTime() == null) {
            return Duration.between(now, remindAt).toMinutes() >= offsetTime.getHour() * 60 + offsetTime.getMinute() + MINUTE_DIFF;
        } else {
            return (remindAt.getDayOfMonth() - now.getDayOfMonth() > 1)
                    || (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1 && now.toLocalTime().plusMinutes(MINUTE_DIFF).isBefore(offsetTime.getLocalTime()));
        }
    }

    public boolean isNeedCreateReminderNotification(LocalDate remindAt, ZoneId zoneId, UserReminderNotification.OffsetTime offsetTime) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(remindAt, offsetTime.getLocalTime(), zoneId);

        return now.isBefore(zonedDateTime);
    }
}
