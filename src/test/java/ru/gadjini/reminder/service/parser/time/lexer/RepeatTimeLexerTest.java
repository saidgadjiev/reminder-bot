package ru.gadjini.reminder.service.parser.time.lexer;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.pattern.Patterns;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.*;

class RepeatTimeLexerTest {

    private static final Locale LOCALE = new Locale("ru");

    private static final TimeLexerConfig TIME_LEXER_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        Mockito.when(TIME_LEXER_CONFIG.getTimePattern(any())).thenReturn(new GroupPattern(Patterns.FIXED_TIME_PATTERN, PatternBuilder.FIXED_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern(any())).thenReturn(new GroupPattern(Patterns.OFFSET_TIME_PATTERN, PatternBuilder.OFFSET_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatTimePattern(any())).thenReturn(new GroupPattern(Patterns.REPEAT_TIME_PATTERN, PatternBuilder.REPEAT_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatWordPattern(any())).thenReturn(new GroupPattern(Patterns.REPEAT_WORD_PATTERN, Collections.emptyList()));
    }

    @Test
    void everyDay() {
        String str = "Тест каждый день в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "1"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест каждый день";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "1")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyDays() {
        String str = "Тест каждые 2 дня в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "2"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест каждые 2 дня";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyMonth() {
        String str = "Тест каждый месяц 25 числа в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "1"), new TimeLexem(DAY, "25"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест каждый месяц 25 числа";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "1"), new TimeLexem(DAY, "25")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyDayOfWeek() {
        String str = "Тест каждый вторник в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY_OF_WEEK, "вторник"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест каждый вторник";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY_OF_WEEK, "вторник")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyYearMonthDay() {
        String str = "Тест каждое 25 сентября в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY, "25"), new TimeLexem(MONTH_WORD, "сентября"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест каждое 25 сентября";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY, "25"), new TimeLexem(MONTH_WORD, "сентября")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyMonths() {
        String str = "Тест каждые 2 месяца 25 числа в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "2"), new TimeLexem(DAY, "25"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест каждые 2 месяца 25 числа";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "2"), new TimeLexem(DAY, "25")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyMinutes() {
        String str = "Тест каждые 10 минут";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MINUTES, "10")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyHours() {
        String str = "Тест каждые 2 часа";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(HOURS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyHoursMinutes() {
        String str = "Тест каждые 2 часа 20 минут";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(HOURS, "2"), new TimeLexem(MINUTES, "20")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void everyMonthsDaysHoursMinutes() {
        String str = "Тест каждые 2 дня 2 часа 20 минут";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "2"), new TimeLexem(HOURS, "2"), new TimeLexem(MINUTES, "20")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void repeatTimes() {
        String str = "Тест каждый вторник в 19:30 среду 20:00";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();

        Assert.assertEquals(expected(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY_OF_WEEK, "вторник"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30"), new TimeLexem(DAY_OF_WEEK, "среду"), new TimeLexem(HOUR, "20"), new TimeLexem(TimeToken.MINUTE, "00")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    private LinkedList<BaseLexem> expected(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }
}