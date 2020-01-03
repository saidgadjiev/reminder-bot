package ru.gadjini.reminder.domain.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Period;
import org.postgresql.util.PGobject;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class RepeatTime {

    public static final String WEEK_DAY = "rt_day_of_week";

    public static final String TIME = "rt_time";

    public static final String INTERVAL = "rt_interval";

    public static final String MONTH = "rt_month";

    public static final String DAY = "rt_day";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DayOfWeek dayOfWeek;

    private Month month;

    private int day;

    private LocalTime time;

    private Period interval;

    private ZoneId zoneId;

    @JsonCreator
    public RepeatTime(@JsonProperty("zoneId") ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepeatTime that = (RepeatTime) o;

        if (day != that.day) return false;
        if (dayOfWeek != that.dayOfWeek) return false;
        if (month != that.month) return false;
        if (!Objects.equals(time, that.time)) return false;
        if (!Objects.equals(interval, that.interval)) return false;
        return Objects.equals(zoneId, that.zoneId);
    }

    @Override
    public int hashCode() {
        int result = dayOfWeek != null ? dayOfWeek.hashCode() : 0;
        result = 31 * result + (month != null ? month.hashCode() : 0);
        result = 31 * result + day;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (interval != null ? interval.hashCode() : 0);
        result = 31 * result + (zoneId != null ? zoneId.hashCode() : 0);
        return result;
    }

    public RepeatTime withZone(ZoneId target) {
        RepeatTime repeatTime = new RepeatTime(target);
        repeatTime.setDayOfWeek(getDayOfWeek());
        repeatTime.setInterval(getInterval());
        repeatTime.setDay(getDay());
        repeatTime.setMonth(getMonth());
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
            sql.append(JodaTimeUtils.toSqlInterval(interval)).append(",");
        }
        if (month == null) {
            sql.append(",");
        } else {
            sql.append(month.name()).append(",");
        }
        if (day != 0) {
            sql.append(day);
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
