package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class FixedTimePatternTest {

    private static final Pattern PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?(((((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс) ?)((?<nextweek>следующ(ий|ей|ую|ее)|след) ?)?)(во|в ?)?)|((((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )|(((?<year>\\d{4})\\.)?(?<month>1[0-2]|[1-9])\\.))((?<day>0[1-9]|[12]\\d|3[01]|0?[1-9]) ?)))?((?<type>до) ?)?");

    @Test
    void matchTimeOnly() {
        String str = StringUtils.reverseDelimited("в 19:00", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeekTime() {
        String str = StringUtils.reverseDelimited("во вторник в 19:00", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeek() {
        String str = StringUtils.reverseDelimited("во вторник", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchDayOfNextWeekTime() {
        String str = StringUtils.reverseDelimited("в след вторник в 19:00", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfNextWeek() {
        String str = StringUtils.reverseDelimited("в след вторник", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("nextweek", "след"), Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchDayMonthWordTime() {
        String str = StringUtils.reverseDelimited("5 сентября в 19:00", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonthWord() {
        String str = StringUtils.reverseDelimited("5 сентября", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("monthword", "сентября")));
    }

    @Test
    void matchDayMonthTime() {
        String str = StringUtils.reverseDelimited("12.5 в 19:00", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonth() {
        String str = StringUtils.reverseDelimited("12.5", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("day", "5"), Map.entry("month", "12")));
    }

    @Test
    void matchDayMonthYearTime() {
        String str = StringUtils.reverseDelimited("2030.12.5 в 19:00", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayMonthYear() {
        String str = StringUtils.reverseDelimited("2030.12.5", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("year", "2030"), Map.entry("day", "5"), Map.entry("month", "12")));
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