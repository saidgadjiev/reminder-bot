package ru.gadjini.reminder.util;

import java.time.LocalDateTime;

public class DateUtils {

    private DateUtils() {

    }

    public static LocalDateTime now() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }
}
