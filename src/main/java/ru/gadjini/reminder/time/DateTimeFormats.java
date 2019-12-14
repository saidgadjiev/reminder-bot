package ru.gadjini.reminder.time;

import java.time.format.DateTimeFormatter;

public class DateTimeFormats {

    private DateTimeFormats() { }

    public static final DateTimeFormatter TIMEZONE_LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
}
