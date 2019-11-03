package ru.gadjini.reminder.util;

import ru.gadjini.reminder.service.requestresolver.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.requestresolver.postpone.parser.PostponeAt;
import ru.gadjini.reminder.service.requestresolver.postpone.parser.PostponeOn;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ParsedTime;

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

    public static ZonedDateTime buildRemindAt(ParsedPostponeTime parsedTime, ZonedDateTime remindAt) {
        if (parsedTime.getPostponeOn() != null) {
            ZonedDateTime newZonedTime = ZonedDateTime.of(remindAt.toLocalDateTime(), remindAt.getZone());
            PostponeOn postponeOn = parsedTime.getPostponeOn();

            if (postponeOn.getDay() != null) {
                newZonedTime.plusDays(postponeOn.getDay());
            }
            if (postponeOn.getHour() != null) {
                newZonedTime.plusHours(postponeOn.getHour());
            }
            if (postponeOn.getMinute() != null) {
                newZonedTime.plusMinutes(postponeOn.getMinute());
            }

            return newZonedTime;
        } else {
            PostponeAt postponeAt = parsedTime.getPostponeAt();

            return buildDateTime(postponeAt.getDay(), postponeAt.getAddDays(), postponeAt.getTime(), remindAt.getZone());
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
