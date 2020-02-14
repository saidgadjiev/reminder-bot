package ru.gadjini.reminder.util;

import org.joda.time.Period;

public class TimeUtils {

    private TimeUtils() {
    }

    public static boolean isBigInterval(Period period) {
        return period.getDays() != 0
                || period.getYears() != 0
                || period.getMonths() != 0
                || period.getWeeks() != 0;
    }
}
