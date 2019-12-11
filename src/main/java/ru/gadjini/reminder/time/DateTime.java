package ru.gadjini.reminder.time;

import java.time.*;

public class DateTime {

    public static final String DATE = "dt_date";

    public static final String TIME = "dt_time";

    private ZoneId zoneId;

    private LocalDate localDate;

    private LocalTime localTime;

    public DateTime(ZonedDateTime zonedDateTime) {
        this.zoneId = zonedDateTime.getZone();
        this.localDate = zonedDateTime.toLocalDate();
        this.localTime = zonedDateTime.toLocalTime();
    }

    public DateTime(ZoneId zoneId) {
        this.zoneId = zoneId;
        this.localDate = LocalDate.now(zoneId);
        this.localTime = LocalTime.now();
    }

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

    public DateTime minusDays(int days) {
        localDate = localDate.minusDays(days);

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

    public DateTime withZoneSameInstant(ZoneId targetZone) {
        return new DateTime(toZonedDateTime(targetZone));
    }

    public ZonedDateTime toZonedDateTime() {
        return ZonedDateTime.of(localDate, localTime, zoneId);
    }

    public ZonedDateTime toZonedDateTime(ZoneId targetZone) {
        return toZonedDateTime().withZoneSameInstant(targetZone);
    }

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(localDate, localTime);
    }
}
