package ru.gadjini.reminder.util;

import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeAt;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeOn;
import ru.gadjini.reminder.service.parser.remind.parser.ParsedCustomRemind;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedTime;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ReminderUtils {

    private ReminderUtils() {

    }

    public static ZonedDateTime buildRemindAt(ParsedTime parsedTime, ZoneId zoneId) {
        return buildDateTime(parsedTime.getMonth(), parsedTime.getDay(), parsedTime.getAddDays(), parsedTime.getTime(), zoneId);
    }

    public static ZonedDateTime buildRemindAt(ParsedPostponeTime parsedTime, ZonedDateTime remindAt, ZoneId zoneId) {
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
            PostponeAt postponeAt = parsedTime.getPostponeAt();

            return buildDateTime(postponeAt.getMonth(), postponeAt.getDay(), postponeAt.getAddDays(), postponeAt.getTime(), zoneId);
        }
    }

    public static ZonedDateTime buildRemindTime(ParsedCustomRemind customRemind, ZonedDateTime remindAt, ZoneId zoneId) {
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

    private static ZonedDateTime buildDateTime(Integer month, Integer day, Integer addDays, LocalTime localTime, ZoneId zoneId) {
        ZonedDateTime dateTime = ZonedDateTime.now(zoneId);

        if (month != null) {
            dateTime = dateTime.withMonth(month);
        }
        if (day != null) {
            if (dateTime.getDayOfMonth() > day) {
                dateTime = dateTime.plusMonths(1);
            }
            dateTime = dateTime.withDayOfMonth(day);
        }
        if (localTime != null) {
            if (dateTime.toLocalTime().isAfter(localTime)) {
                dateTime = dateTime.plusDays(1);
            }
            dateTime = dateTime.with(localTime);
        }
        if (addDays != null) {
            dateTime = dateTime.plusDays(addDays);
        }

        return dateTime;
    }
}
