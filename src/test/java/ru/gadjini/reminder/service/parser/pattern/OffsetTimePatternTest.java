package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.Patterns.OFFSET_TIME_PATTERN;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.match;


class OffsetTimePatternTest {

    @Test
    void matchTypeContainsReminderText() {
        String str = "Сходи на почту 12:00";

        Assert.assertFalse(OFFSET_TIME_PATTERN.matcher(str).matches());
    }

    @Test
    void matchHours() {
        String str = "за 2ч";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("hours", "2")));
    }

    @Test
    void matchDays() {
        String str = "за 2д";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2")));
    }

    @Test
    void matchDaysHours() {
        String str = "за 2д 2ч";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2")));
    }

    @Test
    void matchMinutes() {
        String str = "через 10мин";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("minutes", "10")));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "за 2д 2ч 10мин";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "за"), Map.entry("days", "2"), Map.entry("hours", "2"), Map.entry("minutes", "10")));
    }

    @Test
    void matchDaysTime() {
        String str = "через 2д в 13:00";

        match(OFFSET_TIME_PATTERN, str, Map.ofEntries(Map.entry("type", "через"), Map.entry("days", "2"), Map.entry("hour", "13"), Map.entry("minute", "00")));
    }
}