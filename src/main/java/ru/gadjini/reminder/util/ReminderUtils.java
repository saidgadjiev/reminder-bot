package ru.gadjini.reminder.util;

import ru.gadjini.reminder.service.resolver.parser.ParsedRequest;
import ru.gadjini.reminder.service.resolver.parser.ParsedTime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ReminderUtils {

    private ReminderUtils() {

    }

    public static ZonedDateTime buildRemindAt(ParsedTime parsedTime, ZoneId zoneId) {
        if (parsedTime.getDay() != null) {
            return ZonedDateTime.of(LocalDate.now(zoneId).withDayOfMonth(parsedTime.getDay()), parsedTime.getTime(), zoneId);
        } else if (parsedTime.getAddDays() != null) {
            return ZonedDateTime.of(LocalDate.now(zoneId).plusDays(parsedTime.getAddDays()), parsedTime.getTime(), zoneId);
        }

        return ZonedDateTime.of(LocalDate.now(zoneId), parsedTime.getTime(), zoneId);
    }
}
