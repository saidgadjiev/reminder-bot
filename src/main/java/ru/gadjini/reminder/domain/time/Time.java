package ru.gadjini.reminder.domain.time;

import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;

public class Time {

    private RepeatTime repeatTime;

    private FixedTime fixedTime;

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

    public DateTime getFixedDateTime() {
        return fixedTime.getDateTime();
    }

    public FixedTime getFixedTime() {
        return fixedTime;
    }

    public void setFixedTime(FixedTime fixedTime) {
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
