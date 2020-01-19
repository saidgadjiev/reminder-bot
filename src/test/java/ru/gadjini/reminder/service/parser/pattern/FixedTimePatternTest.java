package ru.gadjini.reminder.service.parser.pattern;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.FIXED_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class FixedTimePatternTest {

    @Test
    void matchTimeOnly() {
        String str = "в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));

        str = "в 19";
        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.HOUR, "19")));

        str = "19";
        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.HOUR, "19")));
    }

    @Test
    void matchDayOfWeekTime() {
        String str = "во вторник в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_OF_WEEK_WORD, "вторник"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));
    }

    @Test
    void matchDayOfWeek() {
        String str = "во вторник";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_OF_WEEK_WORD, "вторник")));
    }

    @Test
    void matchDayOfNextWeekTime() {
        String str = "в след вторник в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.NEXT_WEEK, "след"), Map.entry(PatternBuilder.DAY_OF_WEEK_WORD, "вторник"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));
    }

    @Test
    void matchDayOfNextWeek() {
        String str = "в след вторник";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.NEXT_WEEK, "след"), Map.entry(PatternBuilder.DAY_OF_WEEK_WORD, "вторник")));
    }

    @Test
    void matchDayMonthWordTime() {
        String str = "5 сентября в 19:00";
        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH_WORD, "сентября"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));

        str = "5 сентября в 19";
        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH_WORD, "сентября"), Map.entry(PatternBuilder.HOUR, "19")));
    }

    @Test
    void matchDayMonthWord() {
        String str = "5 сентября";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH_WORD, "сентября")));
    }

    @Test
    void matchDayMonthTime() {
        String str = "12.5 в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH, "12"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));

        str = "12.05 в 19";
        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY, "05"), Map.entry(PatternBuilder.MONTH, "12"), Map.entry(PatternBuilder.HOUR, "19")));
    }

    @Test
    void matchDayMonth() {
        String str = "12.5";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH, "12")));
    }

    @Test
    void matchDayMonthYearTime() {
        String str = "2030.12.5 в 19:00";
        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.YEAR, "2030"), Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH, "12"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));

        str = "2030.12.5 19";
        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.YEAR, "2030"), Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH, "12"), Map.entry(PatternBuilder.HOUR, "19")));
    }

    @Test
    void matchDayMonthYear() {
        String str = "2030.12.5";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.YEAR, "2030"), Map.entry(PatternBuilder.DAY, "5"), Map.entry(PatternBuilder.MONTH, "12")));
    }

    @Test
    void matchToday() {
        String str = "сегодня";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_WORD, "сегодня")));
    }

    @Test
    void matchTodayTime() {
        String str = "сегодня в 19:00";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_WORD, "сегодня"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));

        str = "сегодня в 19";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_WORD, "сегодня"), Map.entry(PatternBuilder.HOUR, "19")));
    }

    @Test
    void matchTomorrow() {
        String str = "завтра в 19:00";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_WORD, "завтра"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));
    }

    @Test
    void matchDayAfterTomorrow() {
        String str = "послезавтра в 19:00";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_WORD, "послезавтра"), Map.entry(PatternBuilder.HOUR, "19"), Map.entry(PatternBuilder.MINUTE, "00")));

        str = "послезавтра в 19";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry(PatternBuilder.DAY_WORD, "послезавтра"), Map.entry(PatternBuilder.HOUR, "19")));
    }
}