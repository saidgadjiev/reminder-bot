package ru.gadjini.reminder.service.parser.pattern;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.PatternBuilder.*;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.REPEAT_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class RepeatTimePatternTest {

    @Test
    void matchHours() {
        String str = "каждые 20ч";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_HOURS, "20")));

        str = "каждые 20часа";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_HOURS, "20")));

        str = "каждые 20 ч";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_HOURS, "20")));

        str = "каждые 50 часов";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_HOURS, "50")));
    }

    @Test
    void matchDays() {
        String str = "каждые 20д";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_DAYS, "20")));

        str = "каждые 2дня";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_DAYS, "2")));

        str = "каждые 2 д";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_DAYS, "2")));

        str = "каждые 50 дней";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_DAYS, "50")));
    }

    @Test
    void matchDaysHours() {
        String str = "каждые 20д 20ч";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_DAYS, "20"), Map.entry(SUFFIX_HOURS, "20")));

        str = "каждые 2дня 2часа";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_DAYS, "2"), Map.entry(SUFFIX_HOURS, "2")));

        str = "каждые 2 д 2 ч";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_HOURS, "2")));

        str = "каждые 5 дней 50 ч";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_DAYS, "5"), Map.entry(PREFIX_HOURS, "50")));
    }

    @Test
    void matchEveryHour() {
        String str = "каждый час";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_HOUR, "час")));
    }

    @Test
    void matchEveryDay() {
        String str = "каждый день";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_DAY, "день")));
    }

    @Test
    void matchEveryMinute() {
        String str = "каждую минуту";
        match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_MINUTE, "минуту")));
    }

    @Test
    void matchEveryDayTime() {
        String str = "каждый день в 19:00";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_DAY, "день"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));
    }

    @Test
    void matchDaysTime() {
        String str = "каждые 2 д в 19:00";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_DAYS, "2"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));
    }

    @Test
    void matchDayOfWeek() {
        String str = "каждый вторник";
        match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(DAY_OF_WEEK_WORD, "вторник")));
    }

    @Test
    void matchMinutes() {
        String str = "каждые 10мин";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_MINUTES, "10")));

        str = "каждые 10минут";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(SUFFIX_MINUTES, "10")));

        str = "каждые 10 мин";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_MINUTES, "10")));

        str = "каждые 10 минут";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_MINUTES, "10")));

        str = "каждые 2 минуты";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_MINUTES, "2")));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "каждые 2 д 20 ч 10 мин";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_HOURS, "20"), Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_MINUTES, "10")));

        str = "каждые 2 дня 2 часа 10 минут";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_HOURS, "2"), Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_MINUTES, "10")));
    }

    @Test
    void matchEveryMonthDay() {
        String str = "каждый месяц 20 числа";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_MONTH, "месяц"), Map.entry(EVERY_MONTH_DAY, "20")));
    }

    @Test
    void matchEveryMonthDayTime() {
        String str = "каждый месяц 20 числа в 19:00";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_MONTH, "месяц"), Map.entry(EVERY_MONTH_DAY, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));
    }

    @Test
    void matchEveryMonthWordDay() {
        String str = "каждое 20 сентября";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20")));
    }

    @Test
    void matchEveryMonthWordDayTime() {
        String str = "каждое 20 сентября в 19:00";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));
    }

    @Test
    void matchEveryYearMonthDay() {
        String str = "каждый год 20 сентября";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_YEAR, "год"), Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20")));
    }

    @Test
    void matchEveryYearMonthDayTime() {
        String str = "каждый год 20 сентября в 19:00";
        match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(EVERY_YEAR, "год"), Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));
    }

    @Test
    void matchMonthsDay() {
        String str = "каждые 20 месяцев 20 числа";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(MONTHS, "20"), Map.entry(EVERY_MONTH_DAY, "20")));

        str = "каждые 2месяца 20 числа";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(MONTHS, "2"), Map.entry(EVERY_MONTH_DAY, "20")));

        str = "каждые 2месяца 20числа";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(MONTHS, "2"), Map.entry(EVERY_MONTH_DAY, "20")));
    }

    @Test
    void matchMonthsDayTime() {
        String str = "каждые 2 месяца 20 числа в 19:00";
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(MONTHS, "2"), Map.entry(EVERY_MONTH_DAY, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00")));
    }
}