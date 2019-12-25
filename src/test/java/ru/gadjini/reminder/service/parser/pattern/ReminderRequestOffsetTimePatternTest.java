package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class ReminderRequestOffsetTimePatternTest {

    private static final Pattern PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?((((мин|минут) )?(?<minutes>\\d+)((мин|минут)( )?)?)?((( )?(ч|час[а-я]{0,2}) )?(?<hours>\\d+)((ч|час[а-я]{1,2})( )?)?)?((( )?(д|дн[а-я]{1,2}) )?(?<days>\\d+)(д|дн[а-я]{1,2})?)?) (?<type>через|на|за|накануне)");

    @Test
    void matchHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2ч", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDays() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2д", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2д 2ч", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchMinutes() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта через 10мин", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("minutes", "10")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта за 2д 2ч 10мин", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysTime() {
        String str = StringUtils.reverseDelimited("Сходить на почту через 2д в 13:00", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("days", "2"), Map.entry("hour", "13"), Map.entry("minute", "00")));
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