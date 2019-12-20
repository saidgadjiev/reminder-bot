package ru.gadjini.reminder.domain;

import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;

public class Time {

    private RepeatTime repeatTime;

    private DateTime fixedTime;

    private OffsetTime offsetTime;

    private ZoneId zoneId;

    public Time(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public RepeatTime getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(RepeatTime repeatTime) {
        this.repeatTime = repeatTime;
    }

    public DateTime getFixedTime() {
        return fixedTime;
    }

    public void setFixedTime(DateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    public OffsetTime getOffsetTime() {
        return offsetTime;
    }

    public boolean isRepeatTime() {
        return repeatTime != null;
    }

    public boolean isFixedTime() {
        return fixedTime != null;
    }

    public boolean isOffsetTime() {
        return offsetTime != null;
    }
}
