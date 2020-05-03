package ru.gadjini.reminder.service.ai;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.*;

@Service
public class ReminderNotificationAI {

    private static final int MINUTE_DIFF = 20;

    private DateTimeService timeCreator;

    @Autowired
    public ReminderNotificationAI(DateTimeService timeCreator) {
        this.timeCreator = timeCreator;
    }

    public boolean isNeedCreateItsTimeNotification(ZonedDateTime remindAt) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZone());

        return now.isBefore(remindAt);
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
