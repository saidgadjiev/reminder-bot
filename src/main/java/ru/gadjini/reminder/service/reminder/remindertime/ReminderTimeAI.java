package ru.gadjini.reminder.service.reminder.remindertime;

import org.joda.time.Period;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class ReminderTimeAI {

    private static final int MINUTE_DIFF = 20;

    public boolean isNeedCreateReminderTime(Period period, int minutes) {
        return period.toStandardMinutes().getMinutes() >= minutes + MINUTE_DIFF;
    }

    public boolean isNeedCreateReminderTime(ZonedDateTime remindAt, int minutes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        return Duration.between(now, remindAt).toMinutes() >= minutes + MINUTE_DIFF;
    }

    public boolean isNeedCreateNightBeforeReminderTime(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        return remindAt.getDayOfMonth() > now.getDayOfMonth();
    }

    public boolean isNeedCreateNightBeforeReminderTime(Period period) {
        return period.getDays() > 1;
    }

    public boolean isNeedCreateNightBeforeReminderTime(LocalDate remindAt, ZoneId zoneId) {
        LocalDate now = LocalDate.now(zoneId);

        return remindAt.getDayOfMonth() > now.getDayOfMonth();
    }

    public boolean isNeedCreateReminderTime(LocalDate remindAt, LocalTime localTime, ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(remindAt, localTime, zoneId);

        return now.isBefore(zonedDateTime);
    }
}
