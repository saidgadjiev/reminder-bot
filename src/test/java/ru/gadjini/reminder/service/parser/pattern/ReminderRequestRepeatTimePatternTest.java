package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.PatternBuilder.*;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.*;


class ReminderRequestRepeatTimePatternTest {

    @Test
    void matchHours() {
        String str = "Проверить готовность торта каждые 2ч";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_HOURS, "2"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2часа";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_HOURS, "2"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 20 ч";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "20"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 5 часов";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "5"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDays() {
        String str = "Проверить готовность торта каждые 2д";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "2"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2дня";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "2"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2 д";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "2"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 5 дней";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "5"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHours() {
        String str = "Проверить готовность торта каждые 2д 2ч";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "2"), Map.entry(SUFFIX_HOURS, "2"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2дня 2часа";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "2"), Map.entry(SUFFIX_HOURS, "2"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 2 д 20 ч";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_HOURS, "20"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());

        str = "Проверить готовность торта каждые 50 дней 5 ч";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "50"), Map.entry(PREFIX_HOURS, "5"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchEveryHour() {
        String str = "Проверить готовность торта каждый час";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_HOUR, "час"))));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchEveryDay() {
        String str = "Сходить в зал каждый день";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_DAY, "день"))));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMinute() {
        String str = "Сходить в зал каждую минуту";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_MINUTE, "минуту"))));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchEveryDayTime() {
        String str = "Сходить в зал каждый день в 19:00";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_DAY, "день"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchDaysTime() {
        String str = "Сходить в зал каждый 2 д в 19:00";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "2"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchDayOfWeek() {
        String str = "Сходить в зал каждый вторник";

        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(DAY_OF_WEEK_WORD, "вторник"))));
        Assert.assertEquals("Сходить в зал", str.substring(0, end).trim());
    }

    @Test
    void matchMinutes() {
        String str = "Готовность торта каждые 10мин";
        int end = repeatMatch(REPEAT_TIME_PATTERN, REPEAT_WORD_PATTERN, str, Collections.emptyList(), List.of(Map.ofEntries(Map.entry(SUFFIX_MINUTES, "10"))));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 10минут";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MINUTES, "10"))));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 10 мин";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MINUTES, "10"))));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 10 минут";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MINUTES, "10"))));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 2 минуты";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MINUTES, "2"))));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "Готовность торта каждые 2 д 2 ч 10 мин";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "2"), Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_MINUTES, "10"))));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());

        str = "Готовность торта каждые 2 дня 2 часа 10 минут";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "2"), Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_MINUTES, "10"))));
        Assert.assertEquals("Готовность торта", str.substring(0, end).trim());
    }


    @Test
    void matchEveryMonthDay() {
        String str = "День рожденье каждый месяц 20 числа";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_MONTH, "месяц"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMonthDayTime() {
        String str = "День рожденье каждый месяц 20 числа в 19:00";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_MONTH, "месяц"), Map.entry(PREFIX_DAY_OF_MONTH, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMonthWordDay() {
        String str = "День рожденье каждое 20 сентября";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry("day", "20"))));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryMonthWordDayTime() {
        String str = "День рожденье каждое 20 сентября в 19:00";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry("day", "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryYearMonthDay() {
        String str = "День рожденье каждый год 20 сентября";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_YEAR, "год"), Map.entry(MONTH_WORD, "сентября"), Map.entry("day", "20"))));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchEveryYearMonthDayTime() {
        String str = "День рожденье каждый год 20 сентября в 19:00";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_YEAR, "год"), Map.entry(MONTH_WORD, "сентября"), Map.entry("day", "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));
        Assert.assertEquals("День рожденье", str.substring(0, end).trim());
    }

    @Test
    void matchMonthsDay() {
        String str = "Идти на работу каждые 2 месяца 20 числа";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "2"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());

        str = "Идти на работу каждые 2месяца 20 числа";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MONTHS, "2"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());

        str = "Идти на работу каждые 2месяца 20числа";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MONTHS, "2"), Map.entry(SUFFIX_DAY_OF_MONTH, "20"))));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());
    }

    @Test
    void matchMonthsDayTime() {
        String str = "Идти на работу каждые 2 месяца 20 числа в 19:00";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "2"), Map.entry(PREFIX_DAY_OF_MONTH, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());

        str = "Идти на работу каждые 2 месяца 20 числа в 19";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "2"), Map.entry(PREFIX_DAY_OF_MONTH, "20"), Map.entry(HOUR, "19"))));
        Assert.assertEquals("Идти на работу", str.substring(0, end).trim());
    }

    @Test
    void matchWeeksDayOfWeek() {
        String str = "Тест каждую неделю во вторник";
        int end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_WEEK, "неделю"), Map.entry(WEEKS_DAY_OF_WEEK_WORD, "вторник"))));
        Assert.assertEquals("Тест", str.substring(0, end).trim());

        str = "Тест каждую 2 недели в субботу";
        end = repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_WEEKS, "2"), Map.entry(WEEKS_DAY_OF_WEEK_WORD, "субботу"))));
        Assert.assertEquals("Тест", str.substring(0, end).trim());
    }

    @Test
    void matchRepeatRepeat() {
        String str = "Тест каждый вторник 19:00 среду 20:00 пятницу";
        int end = repeatTimeMatch(str, Arrays.asList(Map.of(DAY_OF_WEEK_WORD, "пятницу"), Map.of(DAY_OF_WEEK_WORD, "среду", HOUR, "20", MINUTE, "00"), Map.of(DAY_OF_WEEK_WORD, "вторник", HOUR, "19", MINUTE, "00")));
        Assert.assertEquals("Тест", str.substring(0, end).trim());
    }

    @Test
    void matchRepeatWithoutTime() {
        String str = "Тест повторять";
        int end = repeatTimeMatch(str, Collections.emptyList());
        Assert.assertEquals("Тест", str.substring(0, end).trim());
    }
}