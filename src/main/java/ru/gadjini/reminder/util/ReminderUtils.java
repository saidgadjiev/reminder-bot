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

                if (customRemind.getHours() != 0) {
                    now = now.plusHours(customRemind.getHours());
                }
                if (customRemind.getMinutes() != 0) {
                    now = now.plusMinutes(customRemind.getMinutes());
                }

                return now;
            }
            case BEFORE: {
                ZonedDateTime reminderTime = remindAt;

                if (customRemind.getHours() != 0) {
                    reminderTime = reminderTime.minusHours(customRemind.getHours());
                }
                if (customRemind.getMinutes() != 0) {
                    reminderTime = reminderTime.minusMinutes(customRemind.getMinutes());
                }

                return reminderTime;
            }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
