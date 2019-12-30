package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.FIXED_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class ReminderRequestFixedTimePatternTest {

    @Test
    void matchTimeOnly() {
        String str = "Сходить на почту в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayOfWeekTime() {
        String str = "Сходить на почту во вторник в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayOfWeek() {
        String str = "Сходить на почту во вторник";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayOfNextWeekTime() {
        String str = "Сходить на почту в след вторник в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayOfNextWeek() {
        String str = "Сходить на почту в след вторник";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayMonthWordTime() {
        String str = "Сходить на почту 5 сентября в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayMonthWord() {
        String str = "Сходить на почту 5 сентября";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayMonthTime() {
        String str = "Сходить на почту 12.5 в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayMonth() {
        String str = "Сходить на почту 12.5";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayMonthYearTime() {
        String str = "Сходить на почту 2030.12.5 в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayMonthYear() {
        String str = "Сходить на почту 2030.12.5";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchToday() {
        String str = "Сходить на почту сегодня";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchTodayTime() {
        String str = "Сходить на почту сегодня в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchTomorrow() {
        String str = "Сходить на почту завтра в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "завтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchDayAfterTomorrow() {
        String str = "Сходить на почту послезавтра в 19:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "послезавтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchWithDayWordReminderText() {
        String str = "Завтрак в 11:00";

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("hour", "11"), Map.entry("minute", "00")));
        Assert.assertEquals("Завтрак", str.substring(0, end).trim());
    }
}