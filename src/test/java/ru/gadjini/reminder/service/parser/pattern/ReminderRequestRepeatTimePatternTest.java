package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.REPEAT_TIME_PATTERN;


class ReminderRequestRepeatTimePatternTest {

    @Test
    void matchHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2ч", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2часа", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2 ч", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 5 часов", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "5")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDays() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2д", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2дня", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2 д", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 5 дней", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "5")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHours() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2д 2ч", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2дня 2часа", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 2 д 2 ч", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Проверить готовность торта каждые 5 дней 5 ч", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "5"), Map.entry("hours", "5")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryHour() {
        String str = StringUtils.reverseDelimited("Проверить готовность торта каждый час", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyhour", "час")));
        Assert.assertEquals("Проверить готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryDay() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый день", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyday", "день")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryMinute() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждую минуту", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyminute", "минуту")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryDayTime() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый день в 19:00", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyday", "день"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysTime() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый 2 д в 19:00", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDayOfWeek() {
        String str = StringUtils.reverseDelimited("Сходить в зал каждый вторник", ' ');

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
        Assert.assertEquals("Сходить в зал", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchMinutes() {
        String str = StringUtils.reverseDelimited("Готовность торта каждые 10мин", ' ');
        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 10минут", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 10 мин", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 10 минут", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = StringUtils.reverseDelimited("Готовность торта каждые 2 д 2 ч 10 мин", ' ');
        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Готовность торта каждые 2 дня 2 часа 10 минут", ' ');
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", StringUtils.reverseDelimited(str.substring(end), ' '));
    }


    @Test
    void matchEveryMonthDay() {
        String str = StringUtils.reverseDelimited("День рожденье каждый месяц 20 числа", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everymonth", "месяц"), Map.entry("day", "20")));
        Assert.assertEquals("День рожденье", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryMonthDayTime() {
        String str = StringUtils.reverseDelimited("День рожденье каждый месяц 20 числа в 19:00", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everymonth", "месяц"), Map.entry("day", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("День рожденье", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryMonthWordDay() {
        String str = StringUtils.reverseDelimited("День рожденье каждое 20 сентября", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20")));
        Assert.assertEquals("День рожденье", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryMonthWordDayTime() {
        String str = StringUtils.reverseDelimited("День рожденье каждое 20 сентября в 19:00", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("День рожденье", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryYearMonthDay() {
        String str = StringUtils.reverseDelimited("День рожденье каждый год 20 сентября", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyyear", "год"), Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20")));
        Assert.assertEquals("День рожденье", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchEveryYearMonthDayTime() {
        String str = StringUtils.reverseDelimited("День рожденье каждый год 20 сентября в 19:00", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyyear", "год"), Map.entry("monthword", "сентября"), Map.entry("dayofmonthword", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("День рожденье", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchMonthsDay() {
        String str = StringUtils.reverseDelimited("Идти на работу каждые 2 месяца 20 числа", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20")));
        Assert.assertEquals("Идти на работу", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Идти на работу каждые 2месяца 20 числа", ' ');
        end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20")));
        Assert.assertEquals("Идти на работу", StringUtils.reverseDelimited(str.substring(end), ' '));

        str = StringUtils.reverseDelimited("Идти на работу каждые 2месяца 20числа", ' ');
        end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20")));
        Assert.assertEquals("Идти на работу", StringUtils.reverseDelimited(str.substring(end), ' '));
    }

    @Test
    void matchMonthsDayTime() {
        String str = StringUtils.reverseDelimited("Идти на работу каждые 2 месяца 20 числа в 19:00", ' ');
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Идти на работу", StringUtils.reverseDelimited(str.substring(end), ' '));
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