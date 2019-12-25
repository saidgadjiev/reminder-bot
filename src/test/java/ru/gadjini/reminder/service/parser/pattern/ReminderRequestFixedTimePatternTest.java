package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.FIXED_TIME_PATTERN;


class ReminderRequestFixedTimePatternTest {

    @Test
    void matchTimeOnly() {
        String str = StringUtils.reverseDelimited("Сходить на почту в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayOfWeekTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту во вторник в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayOfWeek() {
        String str = StringUtils.reverseDelimited("Сходить на почту во вторник", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayOfNextWeekTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту в след вторник в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayOfNextWeek() {
        String str = StringUtils.reverseDelimited("Сходить на почту в след вторник", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayMonthWordTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту 5 сентября в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayMonthWord() {
        String str = StringUtils.reverseDelimited("Сходить на почту 5 сентября", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayMonthTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту 12.5 в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayMonth() {
        String str = StringUtils.reverseDelimited("Сходить на почту 12.5", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayMonthYearTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту 2030.12.5 в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayMonthYear() {
        String str = StringUtils.reverseDelimited("Сходить на почту 2030.12.5", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchToday() {
        String str = StringUtils.reverseDelimited("Сходить на почту сегодня", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchTodayTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту сегодня в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchTomorrow() {
        String str = StringUtils.reverseDelimited("Сходить на почту завтра в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "завтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayAfterTomorrow() {
        String str = StringUtils.reverseDelimited("Сходить на почту послезавтра в 19:00", ' ');

        int end = match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "послезавтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    private int match(Pattern p, String toMatch, Map<String, String> expected) {
        Matcher maxMatcher = p.matcher(toMatch);

        Assert.assertTrue(maxMatcher.find());
        for (String group : expected.keySet()) {
            String g = maxMatcher.group(group);

            Assert.assertEquals(expected.get(group), g);
        }

        return maxMatcher.end();
    }
}