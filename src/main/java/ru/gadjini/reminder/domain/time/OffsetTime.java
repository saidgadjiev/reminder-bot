package ru.gadjini.reminder.domain.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Period;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class OffsetTime {

    private Type type;

    private Period period = new Period();

    private LocalTime time;

    private ZoneId zoneId;

    @JsonCreator
    public OffsetTime(@JsonProperty("zoneId") ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getDays() {
        return period.getDays();
    }

    public void setDays(int days) {
        period = period.withDays(days);
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public int getHours() {
        return period.getHours();
    }

    public void setHours(int hours) {
        period = period.withHours(hours);
    }

    public int getMinutes() {
        return period.getMinutes();
    }

    public void setMinutes(int minutes) {
        period = period.withMinutes(minutes);
    }

    public Period getPeriod() {
        return period;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public OffsetTime withZone(ZoneId target) {
        OffsetTime offsetTime = new OffsetTime(target);
        offsetTime.setMinutes(getMinutes());
        offsetTime.setHours(getHours());
        offsetTime.setType(getType());
        offsetTime.setDays(getDays());
        if (getTime() != null) {
            LocalTime time = ZonedDateTime.of(LocalDate.now(getZoneId()), getTime(), getZoneId()).withZoneSameInstant(target).toLocalTime();
            offsetTime.setTime(time);
        }

        return offsetTime;
    }

    public enum Type {

        AFTER, //через

        BEFORE, //за

        FOR //на
    }
}
