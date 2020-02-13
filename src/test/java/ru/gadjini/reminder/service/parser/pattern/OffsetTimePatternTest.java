package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.PatternBuilder.*;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.OFFSET_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class OffsetTimePatternTest {

    @Test
    void matchTypeContainsReminderText() {
        String str = "Сходи на почту 12:00";

        Assert.assertFalse(OFFSET_TIME_PATTERN.matcher(str).matches());
    }

    @Test
    void matchHours() {
        String str = "за 20ч";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry(SUFFIX_HOURS, "20")));
    }

    @Test
    void matchDays() {
        String str = "за 20д";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry(SUFFIX_DAYS, "20")));
    }

    @Test
    void matchDaysHours() {
        String str = "за 20д 20ч";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry(SUFFIX_DAYS, "20"), Map.entry(SUFFIX_HOURS, "20")));
    }

    @Test
    void matchMinutes() {
        String str = "через 20мин";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry(SUFFIX_MINUTES, "20")));

        str = "через 20 минут";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry(PREFIX_MINUTES, "20")));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "за 20д 20ч 10мин";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry(SUFFIX_DAYS, "20"), Map.entry(SUFFIX_HOURS, "20"), Map.entry(SUFFIX_MINUTES, "10")));
    }

    @Test
    void matchDaysTime() {
        String str = "через 20д в 13:00";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(SUFFIX_DAYS, "20"), Map.entry(HOUR, "13"), Map.entry(MINUTE, "00")));

        str = "через 20д в 13";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(SUFFIX_DAYS, "20"), Map.entry(HOUR, "13")));
    }

    @Test
    void matchEveTime() {
        String str = "накануне в 19:00";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "накануне"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));

        str = "накануне в 19";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "накануне"), Map.entry(HOUR, "19")));
    }

    @Test
    void matchAfterHour() {
        String str = "через час";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_HOUR, "час")));
    }

    @Test
    void matchAfterHourMinutes() {
        String str = "через час 20мин";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_HOUR, "час"), Map.entry(SUFFIX_MINUTES, "20")));

        str = "через час 20 мин";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_HOUR, "час"), Map.entry(PREFIX_MINUTES, "20")));
    }

    @Test
    void matchAfterDay() {
        String str = "через день";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_DAY, "день")));
    }


    @Test
    void matchAfterDayHours() {
        String str = "через день 10ч";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_DAY, "день"), Map.entry(SUFFIX_HOURS, "10")));
    }

    @Test
    void matchAfterDayTime() {
        String str = "через день в 19:00";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_DAY, "день"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));

        str = "через день в 19";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_DAY, "день"), Map.entry(HOUR, "19")));
    }

    @Test
    void matchPrefixDaysHours() {
        String str = "через 20 дней 20 часов";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(PREFIX_DAYS, "20"), Map.entry(PREFIX_HOURS, "20")));

        str = "через 20 дней 20часов";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(PREFIX_DAYS, "20"), Map.entry(SUFFIX_HOURS, "20")));
    }

    @Test
    void matchMonths() {
        String str = "на 2м";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(SUFFIX_MONTHS, "2")));

        str = "на 5 месяцев";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(PREFIX_MONTHS, "5")));
    }

    @Test
    void matchMonthsDays() {
        String str = "на 2м 2 дня";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(SUFFIX_MONTHS, "2"), Map.entry(PREFIX_DAYS, "2")));

        str = "на 2 месяца 2 дня";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(PREFIX_MONTHS, "2"), Map.entry(PREFIX_DAYS, "2")));
    }

    @Test
    void matchYears() {
        String str = "на 2г";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(SUFFIX_YEARS, "2")));

        str = "на 5 лет";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(PREFIX_YEARS, "5")));
    }

    @Test
    void matchYearsMonths() {
        String str = "на 2г 20 месяцев";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(SUFFIX_YEARS, "2"), Map.entry(PREFIX_MONTHS, "20")));

        str = "на 5 лет 10м";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(PREFIX_YEARS, "5"), Map.entry(SUFFIX_MONTHS, "10")));
    }

    @Test
    void matchWeeks() {
        String str = "на 2 недели";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(PREFIX_WEEKS, "2")));

        str = "на 2недели";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(SUFFIX_WEEKS, "2")));

        str = "на 2н";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(SUFFIX_WEEKS, "2")));

        str = "через 2 н";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(PREFIX_WEEKS, "2")));
    }

    @Test
    void matchMonthsWeeksDaysHours() {
        String str = "на 2м 2н 2 дня 2ч";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(SUFFIX_MONTHS, "2"), Map.entry(SUFFIX_WEEKS, "2"), Map.entry(PREFIX_DAYS, "2"), Map.entry(SUFFIX_HOURS, "2")));

        str = "на 2 месяца 2 недели 2 дня 5 часов";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "на"), Map.entry(PREFIX_MONTHS, "2"), Map.entry(PREFIX_WEEKS, "2"), Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_HOURS, "5")));
    }

    @Test
    void matchAfterMinute() {
        String str = "через минуту";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_MINUTE, "минуту")));
    }

    @Test
    void matchAfterWeek() {
        String str = "через неделю";
        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_WEEK, "неделю")));
    }
}