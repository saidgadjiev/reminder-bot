package ru.gadjini.reminder.service.reminder.notification;

import org.joda.time.Period;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class ReminderNotificationAI {

    private static final int MINUTE_DIFF = 20;

    private static final int ITS_TIME_MINUTE_DIFF = 5;

    public boolean isNeedCreateItsTimeNotification(ZonedDateTime remindAt) {
        ZonedDateTime now = TimeUtils.now(remindAt.getZone());

        return now.plusMinutes(ITS_TIME_MINUTE_DIFF).isBefore(remindAt);
    }

    public boolean isNeedCreateReminderNotification(Period period, UserReminderNotification offsetTime) {
        return period.toStandardMinutes().getMinutes() > offsetTime.getHours() * 60 + offsetTime.getMinutes() + MINUTE_DIFF;
    }

    public boolean isNeedCreateReminderNotification(ZonedDateTime remindAt, UserReminderNotification offsetTime) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (offsetTime.getTime() == null) {
            return Duration.between(now, remindAt).toMinutes() > offsetTime.getHours() * 60 + offsetTime.getMinutes() + MINUTE_DIFF;
        } else {
            return remindAt.getDayOfMonth() - now.getDayOfMonth() > 1 || remindAt.getDayOfMonth() - now.getDayOfMonth() == 1 && now.toLocalTime().plusMinutes(MINUTE_DIFF).isBefore(offsetTime.getTime());
        }
    }

    public boolean isNeedCreateReminderNotification(LocalDate remindAt, UserReminderNotification offsetTime) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(remindAt, offsetTime.getTime(), ZoneOffset.UTC);

        return now.isBefore(zonedDateTime);
    }
}
