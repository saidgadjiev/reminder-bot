package ru.gadjini.reminder.service.reminder.remindertime;

import org.joda.time.Period;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
public class ReminderTimeAI {

    private static final int MINUTE_DIFF = 20;

    public boolean isNeedCreateReminderTime(Period period, int minutes) {
        return period.toStandardMinutes().getMinutes() >= minutes + MINUTE_DIFF;
    }

    public boolean isNeedCreateReminderTime(ZonedDateTime remindAt, int minutes) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        return Duration.between(remindAt, now).toMinutes() >= minutes + MINUTE_DIFF;
    }

    public boolean isNeedCreateNightBeforeReminderTime(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        return remindAt.getDayOfMonth() > now.getDayOfMonth();
    }
}
