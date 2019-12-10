package ru.gadjini.reminder.service.reminder.time;

import org.joda.time.Period;
import org.springframework.stereotype.Service;

@Service
public class IntervalLocalisationService {

    public String get(Period period) {
        StringBuilder time = new StringBuilder();

        time.append(getRepeatWord(period)).append(" ");
        if (period.getDays() != 0) {
            time.append(getDaysPart(period.getDays()));
        }
        if (period.getHours() != 0) {
            time.append(getHoursPart(period.getHours())).append(" ");
        }
        if (period.getMinutes() != 0) {
            time.append(getMinutesPart(period.getMinutes())).append(" ");
        }

        return time.toString().trim();
    }

    private String getRepeatWord(Period period) {
        if (period.getHours() == 1) {
            return "каждый";
        }
        if (period.getMinutes() == 1) {
            return "каждую";
        }

        return "каждые";
    }

    private String getDaysPart(int days) {
        if (days == 1) {
            return "день";
        }

        return days+  " дня";
    }

    private String getMinutesPart(int minutes) {
        if (minutes == 1) {
            return "минуту";
        }

        return minutes + " минут";
    }

    private String getHoursPart(int hours) {
        if (hours == 1) {
            return "час";
        }

        return hours + "часа";
    }
}
