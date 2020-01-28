package ru.gadjini.reminder.bot.command.callback.postpone;

import ru.gadjini.reminder.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class DateTimeData {

    private ZoneId zoneId;

    private LocalDate localDate;

    private LocalTime localTime;

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public static DateTime to(DateTimeData dateTimeData) {
        if (dateTimeData == null) {
            return null;
        }
        DateTime dateTime = new DateTime(dateTimeData.getZoneId());

        dateTime.date(dateTimeData.getLocalDate());
        dateTime.time(dateTimeData.getLocalTime());

        return dateTime;
    }

    public static DateTimeData from(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeData dateTimeData = new DateTimeData();

        dateTimeData.setLocalDate(dateTime.date());
        dateTimeData.setLocalTime(dateTime.time());
        dateTimeData.setZoneId(dateTime.getZoneId());

        return dateTimeData;
    }
}
