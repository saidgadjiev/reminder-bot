package ru.gadjini.reminder.service.parser.pattern;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.gadjini.reminder.service.parser.pattern.PatternBuilder.*;
import static ru.gadjini.reminder.service.parser.pattern.Patterns.*;


class RepeatTimePatternTest {

    @Test
    void matchHours() {
        String str = "каждые 20ч";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_HOURS, "20"))));

        str = "каждые 20часа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_HOURS, "20"))));

        str = "каждые 20 ч";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "20"))));

        str = "каждые 50 часов";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "50"))));
    }

    @Test
    void matchDays() {
        String str = "каждые 20д";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "20"))));

        str = "каждые 2дня";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "2"))));

        str = "каждые 2 д";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "2"))));

        str = "каждые 50 дней";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "50"))));
    }

    @Test
    void matchDaysHours() {
        String str = "каждые 20д 20ч";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "20"), Map.entry(SUFFIX_HOURS, "20"))));

        str = "каждые 2дня 2часа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "2"), Map.entry(SUFFIX_HOURS, "2"))));

        str = "каждые 2 д 2 ч";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_HOURS, "2"))));

        str = "каждые 5 дней 50 ч";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "5"), Map.entry(PREFIX_HOURS, "50"))));
    }

    @Test
    void matchEveryHour() {
        String str = "каждый час";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_HOUR, "час"))));

        str = "каждый 1 час";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "1"))));

        str = "каждый 1ч";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_HOURS, "1"))));
    }

    @Test
    void matchEveryDay() {
        String str = "каждый день";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_DAY, "день"))));

        str = "каждый 1 день";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "1"))));

        str = "каждый 1д";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_DAYS, "1"))));
    }

    @Test
    void matchEveryMinute() {
        String str = "каждую минуту";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_MINUTE, "минуту"))));

        str = "каждую 1 минуту";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MINUTES, "1"))));

        str = "каждую 1мин";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MINUTES, "1"))));
    }

    @Test
    void matchEveryDayTime() {
        String str = "каждый день в 19:00";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_DAY, "день"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));

        str = "каждый день в 19";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_DAY, "день"), Map.entry(HOUR, "19"))));
    }

    @Test
    void matchDaysTime() {
        String str = "каждые 2 д в 19:00";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "2"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));

        str = "каждые 2 д 19";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_DAYS, "2"), Map.entry(HOUR, "19"))));
    }

    @Test
    void matchDayOfWeek() {
        String str = "каждый вторник";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(DAY_OF_WEEK_WORD, "вторник"))));
    }

    @Test
    void matchMinutes() {
        String str = "каждые 10мин";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MINUTES, "10"))));

        str = "каждые 10минут";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MINUTES, "10"))));

        str = "каждые 10 мин";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MINUTES, "10"))));

        str = "каждые 10 минут";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MINUTES, "10"))));

        str = "каждые 2 минуты";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MINUTES, "2"))));
    }

    @Test
    void matchDaysHoursMinutes() {
        String str = "каждые 2 д 20 ч 10 мин";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "20"), Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_MINUTES, "10"))));

        str = "каждые 2 дня 2 часа 10 минут";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_HOURS, "2"), Map.entry(PREFIX_DAYS, "2"), Map.entry(PREFIX_MINUTES, "10"))));
    }

    @Test
    void matchEveryMonthDay() {
        String str = "каждый месяц 20 числа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_MONTH, "месяц"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));

        str = "каждый 1 месяц 20 числа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "1"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));

        str = "каждый 1м 20 числа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MONTHS, "1"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));
    }

    @Test
    void matchEveryMonthDayTime() {
        String str = "каждый месяц 20 числа в 19:00";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_MONTH, "месяц"), Map.entry(PREFIX_DAY_OF_MONTH, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));

        str = "каждый месяц 20 числа в 19";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_MONTH, "месяц"), Map.entry(PREFIX_DAY_OF_MONTH, "20"), Map.entry(HOUR, "19"))));
    }

    @Test
    void matchEveryMonthWordDay() {
        String str = "каждое 20 сентября";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"))));
    }

    @Test
    void matchEveryMonthWordDayTime() {
        String str = "каждое 20 сентября в 19:00";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));

        str = "каждое 20 сентября в 19";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"), Map.entry(HOUR, "19"))));
    }

    @Test
    void matchEveryYearMonthDay() {
        String str = "каждый год 20 сентября";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_YEAR, "год"), Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"))));
    }

    @Test
    void matchEveryYearMonthDayTime() {
        String str = "каждый год 20 сентября в 19:00";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_YEAR, "год"), Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));

        str = "каждый год 20 сентября в 19";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_YEAR, "год"), Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"), Map.entry(HOUR, "19"))));

        str = "каждое 20 сентября в 19";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(MONTH_WORD, "сентября"), Map.entry(PatternBuilder.DAY, "20"), Map.entry(HOUR, "19"))));
    }

    @Test
    void matchMonthsDay() {
        String str = "каждые 20 месяцев 20 числа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "20"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));

        str = "каждые 2месяца 20 числа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MONTHS, "2"), Map.entry(PREFIX_DAY_OF_MONTH, "20"))));

        str = "каждые 2месяца 20числа";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MONTHS, "2"), Map.entry(SUFFIX_DAY_OF_MONTH, "20"))));
    }

    @Test
    void matchMonths() {
        String str = "каждые 20 месяцев";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "20"))));

        str = "каждые 20м";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MONTHS, "20"))));
    }

    @Test
    void matchMonthsDayTime() {
        String str = "каждые 2 месяца 20 числа в 19:00";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "2"), Map.entry(PREFIX_DAY_OF_MONTH, "20"), Map.entry(HOUR, "19"), Map.entry(MINUTE, "00"))));

        str = "каждые 2 месяца 20 числа 19";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "2"), Map.entry(PREFIX_DAY_OF_MONTH, "20"), Map.entry(HOUR, "19"))));
    }

    @Test
    void matchWeeks() {
        String str = "каждые 2 недели";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_WEEKS, "2"))));

        str = "каждые 2н";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_WEEKS, "2"))));
    }

    @Test
    void matchMonthsDaysHoursMinutes() {
        String str = "каждые 20 месяцев 20 недель 20 дней 20 часов 20 минут";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(PREFIX_MONTHS, "20"), Map.entry(PREFIX_WEEKS, "20"), Map.entry(PREFIX_DAYS, "20"), Map.entry(PREFIX_HOURS, "20"), Map.entry(PREFIX_MINUTES, "20"))));

        str = "каждые 20м 20н 20д 20ч 20мин";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(SUFFIX_MONTHS, "20"), Map.entry(SUFFIX_WEEKS, "20"), Map.entry(SUFFIX_DAYS, "20"), Map.entry(SUFFIX_HOURS, "20"), Map.entry(SUFFIX_MINUTES, "20"))));
    }

    @Test
    void matchEveryWeek() {
        String str = "каждую неделю";
        match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(ONE_WEEK, "неделю")));
    }

    @Test
    void matchWeeksDayOfWeek() {
        String str = "каждую неделю во вторник";
        match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(ONE_WEEK, "неделю"), Map.entry(WEEKS_DAY_OF_WEEK_WORD, "вторник")));

        str = "каждую 2 недели в субботу";
        match(REPEAT_TIME_PATTERN, str, Map.ofEntries(Map.entry(PREFIX_WEEKS, "2"), Map.entry(WEEKS_DAY_OF_WEEK_WORD, "субботу")));
    }

    @Test
    void matchRepeatRepeat() {
        String str = "каждый вторник 19:00 среду 20:00 пятницу";
        repeatTimeMatch(str, Arrays.asList(Map.of(DAY_OF_WEEK_WORD, "пятницу"), Map.of(DAY_OF_WEEK_WORD, "среду", HOUR, "20", MINUTE, "00"), Map.of(DAY_OF_WEEK_WORD, "вторник", HOUR, "19", MINUTE, "00")));

        str = "каждый день 19:00 20:00";
        repeatTimeMatch(str, Arrays.asList(Map.of(HOUR, "20", MINUTE, "00"), Map.of(HOUR, "19", MINUTE, "00")));
    }

    @Test
    void matchRepeatWithoutTime() {
        String str = "повторять";
        repeatTimeMatch(str, Collections.emptyList());
    }

    @Test
    void matchSeriesToComplete() {
        String str = "каждый день 5 раз";
        repeatTimeMatch(str, List.of(Map.ofEntries(Map.entry(ONE_DAY, "день"), Map.entry(SERIES_TO_COMPLETE, "5"))));

        str = "каждый день 5 раз вторник 4 раза";
        repeatTimeMatch(str, List.of(Map.of(DAY_OF_WEEK_WORD, "вторник", SERIES_TO_COMPLETE, "4"), Map.of(ONE_DAY, "день", SERIES_TO_COMPLETE, "5")));
    }
}