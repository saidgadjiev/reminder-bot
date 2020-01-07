package ru.gadjini.reminder.util;

import org.joda.time.Period;
import org.postgresql.util.PGInterval;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class JodaTimeUtils {

    private JodaTimeUtils() {

    }

    public static String toSqlInterval(Period period) {
        StringBuilder interval = new StringBuilder();

        interval.append(period.getYears()).append(" years ");
        interval.append(period.getMonths()).append(" months ");
        interval.append(period.getDays()).append(" days ");
        interval.append(period.getHours()).append(" hours ");
        interval.append(period.getMinutes()).append(" minute");

        return interval.toString();
    }

    public static PGInterval toPgInterval(Period period) {
        if (period == null) {
            return null;
        }

        return new PGInterval(period.getYears(), period.getMonths(), period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());
    }

    public static Period toPeriod(PGInterval interval) {
        if (interval == null) {
            return null;
        }

        return new Period(interval.getYears(), interval.getMonths(), 0, interval.getDays(), interval.getHours(), interval.getMinutes(), 0, 0);
    }

    public static ZonedDateTime plus(ZonedDateTime zonedDateTime, Period period) {
        return zonedDateTime.plusYears(period.getYears()).plusMonths(period.getMonths()).plusDays(period.getDays()).plusHours(period.getHours()).plusMinutes(period.getMinutes());
    }

    public static ZonedDateTime minus(ZonedDateTime zonedDateTime, Period period) {
        return zonedDateTime.minusYears(period.getYears()).minusMonths(period.getMonths()).minusDays(period.getDays()).minusHours(period.getHours()).minusMinutes(period.getMinutes());
    }

    public static LocalDate minus(LocalDate localDate, Period period) {
        return localDate.minusYears(period.getYears()).minusMonths(period.getMonths()).minusDays(period.getDays());
    }

    public static LocalDate plus(LocalDate localDate, Period period) {
        return localDate.plusYears(period.getYears()).plusMonths(period.getMonths()).plusDays(period.getDays());
    }
}
