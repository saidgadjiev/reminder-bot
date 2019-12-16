package ru.gadjini.reminder.util;

import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeOn;
import ru.gadjini.reminder.service.parser.remind.parser.OffsetTime;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ReminderUtils {

    private ReminderUtils() {
    }

    public static DateTime buildRemindAt(ParsedPostponeTime parsedTime, DateTime remindAt) {
        if (parsedTime.getPostponeOn() != null) {
            PostponeOn postponeOn = parsedTime.getPostponeOn();

            if (postponeOn.getDay() != null) {
                remindAt = remindAt.plusDays(postponeOn.getDay());
            }
            if (postponeOn.getHour() != null) {
                remindAt = remindAt.plusHours(postponeOn.getHour());
            }
            if (postponeOn.getMinute() != null) {
                remindAt = remindAt.plusMinutes(postponeOn.getMinute());
            }

            return remindAt;
        } else {
            return DateTime.of(parsedTime.getPostponeAt());
        }
    }

    public static ZonedDateTime buildRemindTime(OffsetTime customRemind, ZonedDateTime remindAt, ZoneId zoneId) {
        switch (customRemind.getType()) {
            case AFTER: {
                ZonedDateTime now = ZonedDateTime.now(zoneId);

                if (customRemind.getHour() != null) {
                    now = now.plusHours(customRemind.getHour());
                }
                if (customRemind.getMinute() != null) {
                    now = now.plusMinutes(customRemind.getMinute());
                }

                return now;
            }
            case BEFORE: {
                ZonedDateTime reminderTime = remindAt;

                if (customRemind.getHour() != null) {
                    reminderTime = reminderTime.minusHours(customRemind.getHour());
                }
                if (customRemind.getMinute() != null) {
                    reminderTime = reminderTime.minusMinutes(customRemind.getMinute());
                }

                return reminderTime;
            }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
