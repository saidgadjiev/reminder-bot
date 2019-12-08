package ru.gadjini.reminder.domain;

import org.joda.time.Period;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RepeatTime {

    public static final String WEEK_DAY = "week_day";

    public static final String TIME = "t";

    public static final String INTERVAL = "i";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DayOfWeek dayOfWeek;

    private LocalTime time;

    private Period interval;

    public RepeatTime() {
    }

    public RepeatTime(DayOfWeek dayOfWeek, LocalTime time, Period interval) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.interval = interval;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Period getInterval() {
        return interval;
    }

    public void setInterval(Period interval) {
        this.interval = interval;
    }

    public String sql() {
        StringBuilder sql = new StringBuilder("(");

        if (dayOfWeek == null) {
            sql.append(",");
        } else {
            sql.append(dayOfWeek.name()).append(",");
        }
        if (time == null) {
            sql.append(",");
        } else {
            sql.append(time.format(DATE_TIME_FORMATTER)).append(",");
        }
        if (interval != null) {
            sql.append(JodaTimeUtils.toSqlInterval(interval));
        }
        sql.append(")");

        return sql.toString();
    }
}
