package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class ReminderRequestRepeatTimePatternTest {

    private static final Pattern PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?(((((мин|минут) )?(?<minutes>\\d+)((мин|минут)( )?)?)?((( )?(ч|час[а-я]{0,2}) )?(?<hours>\\d+)((ч|час[а-я]{1,2})( )?)?)?((( )?(д|дн[а-я]{1,2}) )?(?<days>\\d+)(д|дн[а-я]{1,2})?)?)|(?<everyhour>час)|(?<everyday>день)|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)) кажд[а-я]{0,2}");

    @Test
    void matchHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2ч", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2часа", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2 ч", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 5 часов", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("hours", "5")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDays() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2д", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2дня", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2 д", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 5 дней", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "5")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2д 2ч", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2дня 2часа", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2 д 2 ч", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 5 дней 5 ч", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "5"), Map.entry("hours", "5")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryHour() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждый час", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("everyhour", "час")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryDay() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый день", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("everyday", "день")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryDayTime() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый день в 19:00", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("everyday", "день"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysTime() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый 2 д в 19:00", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayOfWeek() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый вторник", ' ');

        int end = match(PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchMinutes() {
        String str = StringUtils.reverseDelimited("Готовность торта каждые 10мин", ' ');
        int end = match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 10минут", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 10 мин", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 10 минут", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = StringUtils.reverseDelimited("Готовность торта каждые 2 д 2 ч 10 мин", ' ');
        int end = match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 2 дня 2 часа 10 минут", ' ');
        end = match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
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