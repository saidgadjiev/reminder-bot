package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

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

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDays() {
        String str = "Проверить готовность торта за 2д";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHours() {
        String str = "Проверить готовность торта за 2д 2ч";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchMinutes() {
        String str = "Проверить готовность торта через 10мин";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("minutes", "10")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "Проверить готовность торта за 2д 2ч 10мин";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2"), Map.entry("minutes", "10")));
        Assert.assertEquals("Проверить готовность торта", str.substring(0, end).trim());
    }

    @Test
    void matchDaysTime() {
        String str = "Сходить на почту через 2д в 13:00";

        int end = match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("days", "2"), Map.entry("hour", "13"), Map.entry("minute", "00")));
        Assert.assertEquals("Сходить на почту", str.substring(0, end).trim());
    }
}