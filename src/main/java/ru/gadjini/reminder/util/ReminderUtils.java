package ru.gadjini.reminder.util;

import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeOn;
import ru.gadjini.reminder.service.parser.remind.parser.ParsedCustomRemind;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ReminderUtils {

    private ReminderUtils() { }

    public static ZonedDateTime buildRemindAt(ParsedPostponeTime parsedTime, ZonedDateTime remindAt) {
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
            return parsedTime.getPostponeAt();
        }
    }

    public static ZonedDateTime buildCustomRemindTime(ParsedCustomRemind customRemind, ZonedDateTime remindAt, ZoneId zoneId) {
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
            case AT: {
                ZonedDateTime now = ZonedDateTime.now(zoneId);

                if (now.getHour() > customRemind.getHour()) {
                    now.plusDays(1);
                }
                now.with(LocalTime.of(customRemind.getHour(), customRemind.getMinute()));

                return now;
            }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
