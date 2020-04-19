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

    public static boolean isEmptyInterval(Period period) {
        if (period == null) {
            return true;
        }
        int[] values = period.getValues();
        for (int value : values) {
            if (value != 0) {
                return false;
            }
        }

        return true;
    }
}
