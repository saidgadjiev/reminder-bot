package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class RepeatTimePatternTest {

    private static final Pattern PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?(((((мин|минут) )?(?<minutes>\\d+)((мин|минут)( )?)?)?((( )?(ч|час[а-я]{0,2}) )?(?<hours>\\d+)((ч|час[а-я]{1,2})( )?)?)?((( )?(д|дн[а-я]{1,2}) )?(?<days>\\d+)(д|дн[а-я]{1,2})?)?)|(?<everyhour>час)|(?<everyday>день)|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)) кажд[а-я]{0,2}");

    @Test
    void matchHours() {
        String str = StringUtils.reverseDelimited("каждые 2ч", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2часа", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2 ч", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 5 часов", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("hours", "5")));
    }

    @Test
    void matchDays() {
        String str = StringUtils.reverseDelimited("каждые 2д", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "2")));

        str = StringUtils.reverseDelimited("каждые 2дня", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "2")));

        str = StringUtils.reverseDelimited("каждые 2 д", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "2")));

        str = StringUtils.reverseDelimited("каждые 5 дней", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "5")));
    }

    @Test
    void matchDaysHours() {
        String str = StringUtils.reverseDelimited("каждые 2д 2ч", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2дня 2часа", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2 д 2 ч", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 5 дней 5 ч", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "5"), Map.entry("hours", "5")));
    }

    @Test
    void matchEveryHour() {
        String str = StringUtils.reverseDelimited("каждый час", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("everyhour", "час")));
    }

    @Test
    void matchEveryDay() {
        String str = StringUtils.reverseDelimited("каждый день", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("everyday", "день")));
    }

    @Test
    void matchEveryDayTime() {
        String str = StringUtils.reverseDelimited("каждый день в 19:00", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("everyday", "день"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDaysTime() {
        String str = StringUtils.reverseDelimited("каждые 2 д в 19:00", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeek() {
        String str = StringUtils.reverseDelimited("каждый вторник", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchMinutes() {
        String str = StringUtils.reverseDelimited("каждые 10мин", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 10минут", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 10 мин", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 10 минут", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = StringUtils.reverseDelimited("каждые 2 д 2 ч 10 мин", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 2 дня 2 часа 10 минут", ' ');
        match(PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
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