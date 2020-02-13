package ru.gadjini.reminder.service.declension;

import org.joda.time.Period;

import java.time.DayOfWeek;

public interface TimeDeclensionService {

    String getLanguage();

    String day(int days);

    String minute(int minutes);

    String hour(int hours);

    String getRepeatWord(DayOfWeek dayOfWeek);

    String dayOfWeek(DayOfWeek dayOfWeek);

    String getRepeatWord(Period period);

    String months(int months);

    String year(int years);

    String weeks(int weeks);
}
