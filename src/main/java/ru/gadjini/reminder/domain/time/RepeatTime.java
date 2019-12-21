package ru.gadjini.reminder.domain.time;

import org.joda.time.Period;
import org.postgresql.util.PGobject;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class RepeatTime {

    public static final String WEEK_DAY = "week_day";

    public static final String TIME = "rt_time";

    public static final String INTERVAL = "rt_interval";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DayOfWeek dayOfWeek;

    private LocalTime time;

    private Period interval;

    private ZoneId zoneId;

    public RepeatTime(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public ZoneId getZoneId() {
        return zoneId;
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

    public boolean hasDayOfWeek() {
        return dayOfWeek != null;
    }

    public boolean hasTime() {
        return time != null;
    }

    public RepeatTime withZone(ZoneId target) {
        RepeatTime repeatTime = new RepeatTime(target);
        repeatTime.setDayOfWeek(getDayOfWeek());
        repeatTime.setInterval(getInterval());
        if (hasTime()) {
            LocalTime time = ZonedDateTime.of(LocalDate.now(zoneId), getTime(), zoneId).withZoneSameInstant(target).toLocalTime();
            repeatTime.setTime(time);
        }

        return repeatTime;
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

    public PGobject sqlObject() {
        PGobject pGobject = new PGobject();
        pGobject.setType("repeat_time");
        try {
            pGobject.setValue(sql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return pGobject;
    }
}
