package ru.gadjini.reminder.service.parser.pattern;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.FIXED_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class FixedTimePatternTest {

    @Test
    void matchTimeOnly() {
        String str = "в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeekTime() {
        String str = "во вторник в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeek() {
        String str = "во вторник";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchDayOfNextWeekTime() {
        String str = "в след вторник в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfNextWeek() {
        String str = "в след вторник";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchDayMonthWordTime() {
        String str = "5 сентября в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonthWord() {
        String str = "5 сентября";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября")));
    }

    @Test
    void matchDayMonthTime() {
        String str = "12.5 в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonth() {
        String str = "12.5";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12")));
    }

    @Test
    void matchDayMonthYearTime() {
        String str = "2030.12.5 в 19:00";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonthYear() {
        String str = "2030.12.5";

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12")));
    }

    @Test
    void matchToday() {
        String str = "сегодня";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня")));
    }

    @Test
    void matchTodayTime() {
        String str = "сегодня в 19:00";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchTomorrow() {
        String str = "завтра в 19:00";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "завтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayAfterTomorrow() {
        String str = "послезавтра в 19:00";
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "послезавтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }
}