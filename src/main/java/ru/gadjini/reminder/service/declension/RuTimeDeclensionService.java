package ru.gadjini.reminder.service.declension;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@Qualifier(RuTimeDeclensionService.LANG)
public class RuTimeDeclensionService implements TimeDeclensionService {

    public static final String LANG = "ru";

    private static final Locale LOCALE = new Locale(LANG);

    @Override
    public String getLanguage() {
        return LANG;
    }

    @Override
    public String day(int days) {
        if (days == 1) {
            return "день";
        }
        if (days >= 2 && days <= 4) {
            return days + " дня";
        }

        return days + " дней";
    }

    @Override
    public String minute(int minutes) {
        if (minutes == 1) {
            return "минуту";
        }
        if (minutes >= 2 && minutes <= 4) {
            return minutes + " минуты";
        }

        return minutes + " минут";
    }

    @Override
    public String hour(int hours) {
        if (hours == 1) {
            return "час";
        }
        if (hours >= 2 && hours <= 4) {
            return hours + " часа";
        }

        return hours + " часов";
    }

    @Override
    public String getRepeatWord(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case THURSDAY:
                return "каждый";
            case WEDNESDAY:
            case FRIDAY:
            case SATURDAY:
                return "каждую";
            case SUNDAY:
                return "каждое";
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public String dayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case SUNDAY:
            case THURSDAY:
                return dayOfWeek.getDisplayName(TextStyle.FULL, LOCALE);
            case WEDNESDAY:
            case FRIDAY:
            case SATURDAY:
                String thursday = dayOfWeek.getDisplayName(TextStyle.FULL, LOCALE);
                return thursday.substring(0, thursday.length() - 1) + "у";
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public String getRepeatWord(Period period) {
        if (period.getYears() != 0) {
            return getRepeatWordByDaysOrHoursOrMonthOrYears(period.getYears());
        } else if (period.getMonths() != 0) {
            return getRepeatWordByDaysOrHoursOrMonthOrYears(period.getMonths());
        } else if (period.getDays() != 0) {
            return getRepeatWordByDaysOrHoursOrMonthOrYears(period.getDays());
        } else if (period.getHours() != 0) {
            return getRepeatWordByDaysOrHoursOrMonthOrYears(period.getHours());
        } else if (period.getMinutes() != 0) {
            return getRepeatWordByMinutes(period.getMinutes());
        }

        return "";
    }

    @Override
    public String months(int months) {
        if (months == 1) {
            return "месяц";
        }
        if (months >= 2 && months <= 4) {
            return months + " месяца";
        }

        return months + " месяцев";
    }

    @Override
    public String year(int years) {
        if (years == 1) {
            return "год";
        }
        if (years >= 2 && years <= 4) {
            return years + " года";
        }

        return years + " лет";
    }

    private String getRepeatWordByDaysOrHoursOrMonthOrYears(int daysOrHoursOrMonthsOrYears) {
        if (daysOrHoursOrMonthsOrYears == 1) {
            return "каждый";
        }

        return "каждые";
    }

    private String getRepeatWordByMinutes(int minutes) {
        if (minutes == 1) {
            return "каждую";
        }

        return "каждые";
    }
}
