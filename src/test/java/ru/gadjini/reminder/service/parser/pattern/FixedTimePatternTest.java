package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.FIXED_TIME_PATTERN;


class FixedTimePatternTest {

    @Test
    void matchTimeOnly() {
        String str = StringUtils.reverseDelimited("в 19:00", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeekTime() {
        String str = StringUtils.reverseDelimited("во вторник в 19:00", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeek() {
        String str = StringUtils.reverseDelimited("во вторник", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchDayOfNextWeekTime() {
        String str = StringUtils.reverseDelimited("в след вторник в 19:00", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfNextWeek() {
        String str = StringUtils.reverseDelimited("в след вторник", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchDayMonthWordTime() {
        String str = StringUtils.reverseDelimited("5 сентября в 19:00", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonthWord() {
        String str = StringUtils.reverseDelimited("5 сентября", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября")));
    }

    @Test
    void matchDayMonthTime() {
        String str = StringUtils.reverseDelimited("12.5 в 19:00", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonth() {
        String str = StringUtils.reverseDelimited("12.5", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12")));
    }

    @Test
    void matchDayMonthYearTime() {
        String str = StringUtils.reverseDelimited("2030.12.5 в 19:00", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonthYear() {
        String str = StringUtils.reverseDelimited("2030.12.5", ' ');

        match(Patterns.FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12")));
    }

    @Test
    void matchToday() {
        String str = StringUtils.reverseDelimited("сегодня", ' ');
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня")));
    }

    @Test
    void matchTodayTime() {
        String str = StringUtils.reverseDelimited("сегодня в 19:00", ' ');
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "сегодня"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchTomorrow() {
        String str = StringUtils.reverseDelimited("завтра в 19:00", ' ');
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "завтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayAfterTomorrow() {
        String str = StringUtils.reverseDelimited("послезавтра в 19:00", ' ');
        match(FIXED_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayword", "послезавтра"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    private void match(Pattern p, String toMatch, Map<String, String> expected) {
        Matcher maxMatcher = p.matcher(toMatch);

        Assert.assertTrue(maxMatcher.find());
        for (String group : expected.keySet()) {
            String g = maxMatcher.group(group);

            Assert.assertEquals(expected.get(group), g);
        }
    }
}