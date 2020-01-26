package ru.gadjini.reminder.domain.time;

import org.postgresql.util.PGobject;
import ru.gadjini.reminder.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FixedTime {

    private DateTime dateTime;

    private Type type = Type.AT;

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public ZoneId getZone() {
        return dateTime.getZoneId();
    }

    public DateTime dayOfMonth(int dayOfMonth) {
        return dateTime.dayOfMonth(dayOfMonth);
    }

    public int dayOfMonth() {
        return dateTime.dayOfMonth();
    }

    public DateTime plusDays(int days) {
        return dateTime.plusDays(days);
    }

    public DateTime plusHours(int hours) {
        return dateTime.plusHours(hours);
    }

    public DateTime plusMinutes(int minutes) {
        return dateTime.plusMinutes(minutes);
    }

    public DateTime plusMonths(int months) {
        return dateTime.plusMonths(months);
    }

    public DateTime month(int month) {
        return dateTime.month(month);
    }

    public LocalDate date() {
        return dateTime.date();
    }

    public DateTime date(LocalDate localDate) {
        return dateTime.date(localDate);
    }

    public LocalTime time() {
        return dateTime.time();
    }

    public DateTime time(LocalTime localTime) {
        return dateTime.time(localTime);
    }

    public DateTime copy() {
        return dateTime.copy();
    }

    public boolean hasTime() {
        return dateTime.hasTime();
    }

    public DateTime withZoneSameInstant(ZoneId targetZone) {
        return dateTime.withZoneSameInstant(targetZone);
    }

    public static DateTime of(LocalDate localDate, LocalTime localTime, ZoneId zoneId) {
        return DateTime.of(localDate, localTime, zoneId);
    }

    public static DateTime of(ZonedDateTime zonedDateTime) {
        return DateTime.of(zonedDateTime);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public DateTime plusYears(int years) {
        return dateTime.plusYears(years);
    }

    public DateTime year(int year) {
        return dateTime.year(year);
    }

    public enum Type {

        UNTIL,

        AT
    }
}
