package ru.gadjini.reminder.bot.command.callback.postpone;

import org.joda.time.Period;
import ru.gadjini.reminder.domain.time.OffsetTime;

import java.time.LocalTime;
import java.time.ZoneId;

public class OffsetTimeData {

    private OffsetTime.Type type;

    private Period period = new Period();

    private LocalTime time;

    private ZoneId zoneId;

    public OffsetTime.Type getType() {
        return type;
    }

    public void setType(OffsetTime.Type type) {
        this.type = type;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
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

    public static OffsetTime to(OffsetTimeData offsetTimeData) {
        if (offsetTimeData == null) {
            return null;
        }
        OffsetTime offsetTime = new OffsetTime(offsetTimeData.getZoneId());

        offsetTime.setType(offsetTimeData.getType());
        offsetTime.setPeriod(offsetTimeData.getPeriod());
        offsetTime.setTime(offsetTimeData.getTime());

        return offsetTime;
    }

    public static OffsetTimeData from(OffsetTime offsetTime) {
        if (offsetTime == null) {
            return null;
        }
        OffsetTimeData offsetTimeData = new OffsetTimeData();

        offsetTimeData.setType(offsetTime.getType());
        offsetTimeData.setPeriod(offsetTime.getPeriod());
        offsetTimeData.setTime(offsetTime.getTime());
        offsetTimeData.setZoneId(offsetTime.getZoneId());

        return offsetTimeData;
    }
}
