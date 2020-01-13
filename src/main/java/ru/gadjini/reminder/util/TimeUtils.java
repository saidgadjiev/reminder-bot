package ru.gadjini.reminder.util;

import java.time.*;

public class TimeUtils {

    private TimeUtils() { }

    public static ZonedDateTime now(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId).withSecond(0).withNano(0);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }

    public static ZonedDateTime nowZoned() {
        return ZonedDateTime.now().withSecond(0).withNano(0);
    }

    public static LocalTime nowTime(ZoneId zoneId) {
        return LocalTime.now(zoneId).withSecond(0).withNano(0);
    }
}
