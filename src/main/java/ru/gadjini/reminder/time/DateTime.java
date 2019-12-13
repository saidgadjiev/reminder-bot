package ru.gadjini.reminder.time;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTime {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static final String DATE = "dt_date";

    public static final String TIME = "dt_time";

    private ZoneId zoneId;

    private LocalDate localDate;

    private LocalTime localTime;

    public ZoneId getZone() {
        return zoneId;
    }

    public DateTime dayOfMonth(int dayOfMonth) {
        localDate = localDate.withDayOfMonth(dayOfMonth);

        return this;
    }

    public int dayOfMonth() {
        return localDate.getDayOfMonth();
    }

    public DateTime plusDays(int days) {
        localDate = localDate.plusDays(days);

        return this;
    }

    public DateTime plusMonths(int months) {
        localDate = localDate.plusMonths(months);

        return this;
    }

    public DateTime month(int month) {
        localDate = localDate.withMonth(month);

        return this;
    }

    public LocalDate date() {
        return localDate;
    }

    public DateTime date(LocalDate localDate) {
        this.localDate = localDate;

        return this;
    }

    public LocalTime time() {
        return localTime;
    }

    public DateTime time(LocalTime localTime) {
        this.localTime = localTime;

        return this;
    }

    public boolean hasTime() {
        return localTime != null;
    }

    public static DateTime now(ZoneId zoneId) {
        return of(ZonedDateTime.now(zoneId));
    }

    public DateTime withZoneSameInstant(ZoneId targetZone) {
        if (localTime != null) {
            return DateTime.of(toZonedDateTime().withZoneSameInstant(targetZone));
        }

        return DateTime.of(localDate, null, targetZone);
    }

    public ZonedDateTime toZonedDateTime() {
        return ZonedDateTime.of(localDate, localTime, zoneId);
    }

    public String sql() {
        StringBuilder sql = new StringBuilder();

        sql.append("(").append(DATE_FORMATTER.format(localDate)).append(",");
        if (localTime != null) {
            sql.append(TIME_FORMATTER.format(localTime));
        }
        sql.append(")");

        return sql.toString();
    }

    public static DateTime of(LocalDate localDate, LocalTime localTime, ZoneId zoneId) {
        DateTime dateTime = new DateTime();

        dateTime.zoneId = zoneId;
        dateTime.localDate = localDate;
        dateTime.localTime = localTime;

        return dateTime;
    }

    public static DateTime of(ZonedDateTime zonedDateTime) {
        DateTime dateTime = new DateTime();

        dateTime.zoneId = zonedDateTime.getZone();
        dateTime.localDate = zonedDateTime.toLocalDate();
        dateTime.localTime = zonedDateTime.toLocalTime();

        return dateTime;
    }
}
