package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class OffsetTimePatternTest {

    private static final Pattern PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?((((мин|минут) )?(?<minutes>\\d+)((мин|минут)( )?)?)?((( )?(ч|час[а-я]{0,2}) )?(?<hours>\\d+)((ч|час[а-я]{1,2})( )?)?)?((( )?(д|дн[а-я]{1,2}) )?(?<days>\\d+)(д|дн[а-я]{1,2})?)?) (?<type>через|на|за|накануне)");

    @Test
    void matchHours() {
        String str = StringUtils.reverseDelimited("за 2ч", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("hours", "2")));
    }

    @Test
    void matchDays() {
        String str = StringUtils.reverseDelimited("за 2д", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2")));
    }

    @Test
    void matchDaysHours() {
        String str = StringUtils.reverseDelimited("за 2д 2ч", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2")));
    }

    @Test
    void matchMinutes() {
        String str = StringUtils.reverseDelimited("через 10мин", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("minutes", "10")));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = StringUtils.reverseDelimited("за 2д 2ч 10мин", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2"), Map.entry("minutes", "10")));
    }

    @Test
    void matchDaysTime() {
        String str = StringUtils.reverseDelimited("через 2д в 13:00", ' ');

        match(PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("days", "2"), Map.entry("hour", "13"), Map.entry("minute", "00")));
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