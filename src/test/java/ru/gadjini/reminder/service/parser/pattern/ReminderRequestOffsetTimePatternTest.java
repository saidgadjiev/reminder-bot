package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.PatternBuilder.*;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.OFFSET_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class ReminderRequestOffsetTimePatternTest {

    @Test
    void matchFixedTime() {
        String str = "Test завтра в 19:00";
        Assert.assertFalse(OFFSET_TIME_PATTERN.matcher(str).find());
    }

    @Test
    void matchHours() {
        String str = "Проверить готовность торта за 2ч";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "за"), Map.entry(PatternBuilder.SUFFIX_HOURS, "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDays() {
        String str = "Проверить готовность торта за 2д";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "за"), Map.entry(PatternBuilder.SUFFIX_DAYS, "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHours() {
        String str = "Проверить готовность торта за 2д 2ч";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "за"), Map.entry(PatternBuilder.SUFFIX_DAYS, "2"), Map.entry(PatternBuilder.SUFFIX_HOURS, "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchMinutes() {
        String str = "Проверить готовность торта через 10мин";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(PatternBuilder.SUFFIX_MINUTES, "10")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "Проверить готовность торта за 2д 2ч 10мин";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "за"), Map.entry(PatternBuilder.SUFFIX_DAYS, "2"), Map.entry(PatternBuilder.SUFFIX_HOURS, "2"), Map.entry(PatternBuilder.SUFFIX_MINUTES, "10")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysTime() {
        String str = "Сходить на почту через 2д в 13:00";
        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(PatternBuilder.SUFFIX_DAYS, "2"), Map.entry(PatternBuilder.HOUR, "13"), Map.entry(PatternBuilder.MINUTE, "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());

        str = "Сходить на почту через 2д в 13";
        end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(PatternBuilder.SUFFIX_DAYS, "2"), Map.entry(PatternBuilder.HOUR, "13")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }

    @Test
    void matchWeeksDayOfWeek() {
        String str = "Тест через неделю в пятницу";
        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(ONE_WEEK, "неделю"), Map.entry(WEEKS_DAY_OF_WEEK_WORD, "пятницу")));
        Assert.assertEquals("Тест", str.substring(0, end).trim());

        str = "Тест через 2 недели во вторник";
        end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry(TYPE, "через"), Map.entry(PREFIX_WEEKS, "2"), Map.entry(WEEKS_DAY_OF_WEEK_WORD, "вторник")));
        Assert.assertEquals("Тест", str.substring(0, end).trim());
    }
}