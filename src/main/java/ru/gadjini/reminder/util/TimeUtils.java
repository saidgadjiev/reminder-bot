package ru.gadjini.reminder.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
}
