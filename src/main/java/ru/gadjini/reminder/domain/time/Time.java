package ru.gadjini.reminder.domain.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;
import java.util.List;

public class Time {

    private List<RepeatTime> repeatTimes;

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

    public List<RepeatTime> getRepeatTimes() {
        return repeatTimes;
    }

    public void setRepeatTimes(List<RepeatTime> repeatTimes) {
        this.repeatTimes = repeatTimes;
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
        return repeatTimes != null && repeatTimes.size() > 0;
    }

    public boolean isFixedTime() {
        return fixedTime != null;
    }

    public boolean isOffsetTime() {
        return offsetTime != null;
    }
}
