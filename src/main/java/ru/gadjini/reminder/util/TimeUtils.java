package ru.gadjini.reminder.util;

import ru.gadjini.reminder.time.DateTime;

import java.time.*;

public class TimeUtils {

    private TimeUtils() { }

    public static ZonedDateTime zonedDateTimeNow(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId).withSecond(0).withNano(0);
    }

    public static LocalDateTime localDateTimeNow() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }

    public static ZonedDateTime zonedDateTimeNow() {
        return ZonedDateTime.now().withSecond(0).withNano(0);
    }

    public static LocalTime localTimeNow(ZoneId zoneId) {
        return LocalTime.now(zoneId).withSecond(0).withNano(0);
    }

    public static LocalDate localDateNow(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    public static LocalDate localDateNow() {
        return LocalDate.now();
    }

    public static DateTime dateTimeNow(ZoneId zoneId) {
        return DateTime.now(zoneId);
    }
}
