package ru.gadjini.reminder.domain.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;

public class Time {

    private RepeatTime repeatTime;

    private FixedTime fixedTime;

    private OffsetTime offsetTime;

    private ZoneId zoneId;

    @JsonCreator
    public Time(@JsonProperty("zoneId") ZoneId zoneId) {
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

    @JsonIgnore
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
