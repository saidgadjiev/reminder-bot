package ru.gadjini.reminder.util;

import org.joda.time.Period;
import org.postgresql.util.PGInterval;

public class JodaTimeUtils {

    private JodaTimeUtils() {

    }

    public static String toSqlInterval(Period period) {
        StringBuilder interval = new StringBuilder();

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
}
