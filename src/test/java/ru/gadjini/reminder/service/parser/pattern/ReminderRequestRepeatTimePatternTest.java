package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.REPEAT_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class ReminderRequestRepeatTimePatternTest {

    @Test
    void matchHours() {
        String str = "Проверить готовность торта каждые 2ч";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2часа";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2 ч";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 5 часов";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "5")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDays() {
        String str = "Проверить готовность торта каждые 2д";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2дня";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2 д";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 5 дней";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "5")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHours() {
        String str = "Проверить готовность торта каждые 2д 2ч";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2дня 2часа";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2 д 2 ч";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 5 дней 5 ч";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "5"), Map.entry("hours", "5")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchEveryHour() {
        String str = "Проверить готовность торта каждый час";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyhour", "час")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchEveryDay() {
        String str = "Сходить в зал каждый день";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyday", "день")));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMinute() {
        String str = "Сходить в зал каждую минуту";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyminute", "минуту")));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchEveryDayTime() {
        String str = "Сходить в зал каждый день в 19:00";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyday", "день"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchDaysTime() {
        String str = "Сходить в зал каждый 2 д в 19:00";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("days", "2"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchDayOfWeek() {
        String str = "Сходить в зал каждый вторник";

        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("dayofweek", "вторник")));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchMinutes() {
        String str = "Готовность торта каждые 10мин";
        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 10минут";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 10 мин";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 10 минут";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "Готовность торта каждые 2 д 2 ч 10 мин";
        int end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 2 дня 2 часа 10 минут";
        end = match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("hours", "2"), Map.entry("days", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());
    }


    @Test
    void matchEveryMonthDay() {
        String str = "День рожденье каждый месяц 20 числа";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everymonth", "месяц"), Map.entry("everymonthday", "20")));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMonthDayTime() {
        String str = "День рожденье каждый месяц 20 числа в 19:00";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everymonth", "месяц"), Map.entry("everymonthday", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMonthWordDay() {
        String str = "День рожденье каждое 20 сентября";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("monthword", "сентября"), Map.entry("day", "20")));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMonthWordDayTime() {
        String str = "День рожденье каждое 20 сентября в 19:00";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("monthword", "сентября"), Map.entry("day", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryYearMonthDay() {
        String str = "День рожденье каждый год 20 сентября";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyyear", "год"), Map.entry("monthword", "сентября"), Map.entry("day", "20")));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryYearMonthDayTime() {
        String str = "День рожденье каждый год 20 сентября в 19:00";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("everyyear", "год"), Map.entry("monthword", "сентября"), Map.entry("day", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchMonthsDay() {
        String str = "Идти на работу каждые 2 месяца 20 числа";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20")));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());

        str = "Идти на работу каждые 2месяца 20 числа";
        end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20")));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());

        str = "Идти на работу каждые 2месяца 20числа";
        end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20")));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());
    }

    @Test
    void matchMonthsDayTime() {
        String str = "Идти на работу каждые 2 месяца 20 числа в 19:00";
        int end = match(Patterns.REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry("months", "2"), Map.entry("everymonthday", "20"), Map.entry("hour", "19"), Map.entry("minute", "00")));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());
    }
}