package ru.gadjini.reminder.util;

import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeAt;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeOn;
import ru.gadjini.reminder.service.parser.remind.parser.ParsedCustomRemind;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ReminderUtils {

    private ReminderUtils() {

    }

    public static ZonedDateTime buildRemindAt(ParsedTime parsedTime, ZoneId zoneId) {
        return buildDateTime(parsedTime.getDay(), parsedTime.getAddDays(), parsedTime.getTime(), zoneId);
    }

    public static ZonedDateTime buildRemindAt(ParsedPostponeTime parsedTime, ZoneId zoneId) {
        if (parsedTime.getPostponeOn() != null) {
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            PostponeOn postponeOn = parsedTime.getPostponeOn();

            if (postponeOn.getDay() != null) {
                now = now.plusDays(postponeOn.getDay());
            }
            if (postponeOn.getHour() != null) {
                now = now.plusHours(postponeOn.getHour());
            }
            if (postponeOn.getMinute() != null) {
                now = now.plusMinutes(postponeOn.getMinute());
            }

            return now;
        } else {
            PostponeAt postponeAt = parsedTime.getPostponeAt();

            return buildDateTime(postponeAt.getDay(), postponeAt.getAddDays(), postponeAt.getTime(), zoneId);
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

    private static ZonedDateTime buildDateTime(Integer day, Integer addDays, LocalTime localTime, ZoneId zoneId) {
        if (day != null) {
            return ZonedDateTime.of(LocalDate.now(zoneId).withDayOfMonth(day), localTime, zoneId);
        } else if (addDays != null) {
            return ZonedDateTime.of(LocalDate.now(zoneId).plusDays(addDays),localTime, zoneId);
        }

        return ZonedDateTime.of(LocalDate.now(zoneId), localTime, zoneId);
    }
}
