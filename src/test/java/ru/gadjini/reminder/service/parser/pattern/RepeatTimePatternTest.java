package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.REPEAT_TIME_PATTERN;


class RepeatTimePatternTest {

    @Test
    void matchHours() {
        String str = StringUtils.reverseDelimited("каждые 2ч", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2часа", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2 ч", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 5 часов", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "5")));
    }

    @Test
    void matchDays() {
        String str = StringUtils.reverseDelimited("каждые 2д", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));

        str = StringUtils.reverseDelimited("каждые 2дня", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));

        str = StringUtils.reverseDelimited("каждые 2 д", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));

        str = StringUtils.reverseDelimited("каждые 5 дней", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "5")));
    }

    @Test
    void matchDaysHours() {
        String str = StringUtils.reverseDelimited("каждые 2д 2ч", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2дня 2часа", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 2 д 2 ч", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));

        str = StringUtils.reverseDelimited("каждые 5 дней 5 ч", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "5"), Map.entry("hours", "5")));
    }

    @Test
    void matchEveryHour() {
        String str = StringUtils.reverseDelimited("каждый час", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyhour", "час")));
    }

    @Test
    void matchEveryDay() {
        String str = StringUtils.reverseDelimited("каждый день", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyday", "день")));
    }

    @Test
    void matchEveryMinute() {
        String str = StringUtils.reverseDelimited("каждую минуту", ' ');
        match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyminute", "минуту")));
    }

    @Test
    void matchEveryDayTime() {
        String str = StringUtils.reverseDelimited("каждый день в 19:00", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyday", "день"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDaysTime() {
        String str = StringUtils.reverseDelimited("каждые 2 д в 19:00", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchDayOfWeek() {
        String str = StringUtils.reverseDelimited("каждый вторник", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
    }

    @Test
    void matchMinutes() {
        String str = StringUtils.reverseDelimited("каждые 10мин", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 10минут", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 10 мин", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 10 минут", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = StringUtils.reverseDelimited("каждые 2 д 2 ч 10 мин", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));

        str = StringUtils.reverseDelimited("каждые 2 дня 2 часа 10 минут", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
    }

    @Test
    void matchEveryMonthDay() {
        String str = StringUtils.reverseDelimited("каждый месяц 20 числа", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everymonth", "месяц"), Map.entry("day", "20")));
    }

    @Test
    void matchEveryMonthDayTime() {
        String str = StringUtils.reverseDelimited("каждый месяц 20 числа в 19:00", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everymonth", "месяц"), Map.entry("day", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchEveryMonthWordDay() {
        String str = StringUtils.reverseDelimited("каждое 20 сентября", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20")));
    }

    @Test
    void matchEveryMonthWordDayTime() {
        String str = StringUtils.reverseDelimited("каждое 20 сентября в 19:00", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
    }

    @Test
    void matchEveryYearMonthDay() {
        String str = StringUtils.reverseDelimited("каждый год 20 сентября", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyyear", "год"), Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20")));
    }

    @Test
    void matchEveryYearMonthDayTime() {
        String str = StringUtils.reverseDelimited("каждый год 20 сентября в 19:00", ' ');
        match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyyear", "год"), Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
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