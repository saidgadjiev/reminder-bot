package ru.gadjini.reminder.bot.command.callback.postpone;

import org.joda.time.Period;
import ru.gadjini.reminder.domain.time.RepeatTime;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class RepeatTimeData {

    private DayOfWeek dayOfWeek;

    private Month month;

    private int day;

    private LocalTime time;

    private Period interval;

    private ZoneId zoneId;

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public static List<RepeatTime> to(List<RepeatTimeData> repeatTimeData) {
        if (repeatTimeData == null) {
            return null;
        }
        return repeatTimeData.stream().map(RepeatTimeData::to).collect(Collectors.toList());
    }

    public static RepeatTime to(RepeatTimeData repeatTimeData) {
        if (repeatTimeData == null) {
            return null;
        }
        RepeatTime repeatTime = new RepeatTime(repeatTimeData.getZoneId());

        repeatTime.setDay(repeatTimeData.getDay());
        repeatTime.setDayOfWeek(repeatTimeData.getDayOfWeek());
        repeatTime.setInterval(repeatTimeData.getInterval());
        repeatTime.setMonth(repeatTimeData.getMonth());
        repeatTime.setTime(repeatTimeData.getTime());

        return repeatTime;
    }

    public static RepeatTimeData from(RepeatTime repeatTime) {
        if (repeatTime == null) {
            return null;
        }
        RepeatTimeData repeatTimeData = new RepeatTimeData();

        repeatTimeData.setDay(repeatTime.getDay());
        repeatTimeData.setDayOfWeek(repeatTime.getDayOfWeek());
        repeatTimeData.setInterval(repeatTime.getInterval());
        repeatTimeData.setMonth(repeatTime.getMonth());
        repeatTimeData.setTime(repeatTime.getTime());
        repeatTimeData.setZoneId(repeatTime.getZoneId());

        return repeatTimeData;
    }

    public static List<RepeatTimeData> from(List<RepeatTime> repeatTimes) {
        if (repeatTimes == null) {
            return null;
        }

        return repeatTimes.stream().map(RepeatTimeData::from).collect(Collectors.toList());
    }
}
