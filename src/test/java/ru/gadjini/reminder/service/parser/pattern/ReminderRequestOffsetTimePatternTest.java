package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.OFFSET_TIME_PATTERN;


class ReminderRequestOffsetTimePatternTest {

    @Test
    void matchHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2ч", ' ');

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDays() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2д", ' ');

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2д 2ч", ' ');

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchMinutes() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта через 10мин", ' ');

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("minutes", "10")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2д 2ч 10мин", ' ');

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту через 2д в 13:00", ' ');

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("days", "2"), Map.entry("hour", "13"), Map.entry("minute", "00")));
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