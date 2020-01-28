package ru.gadjini.reminder.time;

import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DateTime {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static final String DATE = "dt_date";

    public static final String TIME = "dt_time";

    private ZoneId zoneId;

    private LocalDate localDate;

    private LocalTime localTime;

    public DateTime(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public ZoneId getZoneId() {
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

    public DateTime plusHours(int hours) {
        localTime = localTime.plusHours(hours);

        return this;
    }

    public DateTime plusMinutes(int minutes) {
        localTime = localTime.plusMinutes(minutes);

        return this;
    }

    public DateTime plusMonths(int months) {
        localDate = localDate.plusMonths(months);

        return this;
    }

    public DateTime plusYears(int years) {
        localDate = localDate.plusYears(years);

        return this;
    }

    public DateTime year(int year) {
        localDate = localDate.withYear(year);

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

    public DateTime copy() {
        return DateTime.of(localDate, localTime, zoneId);
    }

    public boolean hasTime() {
        return localTime != null;
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

    public PGobject sqlObject() {
        PGobject pGobject = new PGobject();
        pGobject.setType("datetime");
        try {
            pGobject.setValue(sql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return pGobject;
    }

    public static DateTime of(LocalDate localDate, LocalTime localTime, ZoneId zoneId) {
        DateTime dateTime = new DateTime(zoneId);

        dateTime.localDate = localDate;
        dateTime.localTime = localTime;

        return dateTime;
    }

    public static DateTime of(ZonedDateTime zonedDateTime) {
        DateTime dateTime = new DateTime(zonedDateTime.getZone());

        dateTime.localDate = zonedDateTime.toLocalDate();
        dateTime.localTime = zonedDateTime.toLocalTime();

        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateTime dateTime = (DateTime) o;

        if (!Objects.equals(zoneId, dateTime.zoneId)) return false;
        if (!Objects.equals(localDate, dateTime.localDate)) return false;
        return Objects.equals(localTime, dateTime.localTime);
    }

    @Override
    public int hashCode() {
        int result = zoneId != null ? zoneId.hashCode() : 0;
        result = 31 * result + (localDate != null ? localDate.hashCode() : 0);
        result = 31 * result + (localTime != null ? localTime.hashCode() : 0);
        return result;
    }
}
