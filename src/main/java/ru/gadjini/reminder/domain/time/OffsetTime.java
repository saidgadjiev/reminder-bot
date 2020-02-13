package ru.gadjini.reminder.domain.time;

import org.joda.time.Period;

import java.time.LocalTime;
import java.time.ZoneId;

public class OffsetTime {

    private Type type;

    private Period period = new Period();

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
        return period.getDays();
    }

    public void setDays(int days) {
        period = period.withDays(days);
    }

    public void setWeeks(int weeks) {
        period = period.withWeeks(weeks);
    }

    public void setMonths(int months) {
        period = period.withMonths(months);
    }

    public void setYears(int years) {
        period = period.withYears(years);
    }

    public int getYears() {
        return period.getYears();
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

    public boolean hasTime() {
        return time != null;
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

    public enum Type {

        AFTER, //через

        BEFORE, //за

        FOR //на
    }
}
