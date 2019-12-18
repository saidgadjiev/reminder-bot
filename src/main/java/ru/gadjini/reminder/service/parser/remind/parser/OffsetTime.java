package ru.gadjini.reminder.service.parser.remind.parser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class OffsetTime {

    private Type type;

    private int days;

    private int hours;

    private int minutes;

    private LocalTime time;

    private ZoneId zoneId;

    public OffsetTime(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
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

        AFTER,

        BEFORE
    }
}
