package ru.gadjini.reminder.bot.command.callback.postpone;

import ru.gadjini.reminder.domain.time.Time;

import java.time.ZoneId;
import java.util.List;

public class TimeData {

    private List<RepeatTimeData> repeatTimes;

    private FixedTimeData fixedTime;

    private OffsetTimeData offsetTime;

    private ZoneId zoneId;

    public List<RepeatTimeData> getRepeatTimes() {
        return repeatTimes;
    }

    public void setRepeatTimes(List<RepeatTimeData> repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public FixedTimeData getFixedTime() {
        return fixedTime;
    }

    public void setFixedTime(FixedTimeData fixedTime) {
        this.fixedTime = fixedTime;
    }

    public OffsetTimeData getOffsetTime() {
        return offsetTime;
    }

    public void setOffsetTime(OffsetTimeData offsetTime) {
        this.offsetTime = offsetTime;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public static Time to(TimeData timeData) {
        Time time = new Time(timeData.getZoneId());

        time.setFixedTime(FixedTimeData.to(timeData.getFixedTime()));
        time.setRepeatTimes(RepeatTimeData.to(timeData.getRepeatTimes()));
        time.setOffsetTime(OffsetTimeData.to(timeData.getOffsetTime()));

        return time;
    }

    public static TimeData from(Time time) {
        TimeData timeData = new TimeData();

        timeData.setFixedTime(FixedTimeData.from(time.getFixedTime()));
        timeData.setZoneId(time.getZoneId());
        timeData.setRepeatTimes(RepeatTimeData.from(time.getRepeatTimes()));
        timeData.setOffsetTime(OffsetTimeData.from(time.getOffsetTime()));

        return timeData;
    }
}
