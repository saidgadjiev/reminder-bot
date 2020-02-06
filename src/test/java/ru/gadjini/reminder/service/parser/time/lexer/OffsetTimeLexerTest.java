package ru.gadjini.reminder.service.parser.time.lexer;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.pattern.Patterns;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.*;

class OffsetTimeLexerTest {

    private static final Locale LOCALE = new Locale("ru");

    private static final TimeLexerConfig TIME_LEXER_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        Mockito.when(TIME_LEXER_CONFIG.getTimePattern(any())).thenReturn(new GroupPattern(Patterns.FIXED_TIME_PATTERN, PatternBuilder.FIXED_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern(any())).thenReturn(new GroupPattern(Patterns.OFFSET_TIME_PATTERN, PatternBuilder.OFFSET_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatTimePattern(any())).thenReturn(new GroupPattern(Patterns.REPEAT_TIME_PATTERN, PatternBuilder.REPEAT_TIME_PATTERN_GROUPS));
    }

    @Test
    void afterHour() {
        String str = "Тест через час";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(HOURS, "1")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void afterHours() {
        String str = "Тест через 2 часа";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(HOURS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void afterMinutes() {
        String str = "Тест через 10 минут";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(MINUTES, "10")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void afterDays() {
        String str = "Тест через 2 дня в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(DAYS, "2"), new TimeLexem(HOUR, "19"), new TimeLexem(MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест через 2 дня";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(DAYS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void afterMonth() {
        String str = "Тест через месяц";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(MONTHS, "1")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void afterMonths() {
        String str = "Тест через 2 месяца 2 дня 2 часа";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(MONTHS, "2"), new TimeLexem(DAYS, "2"), new TimeLexem(HOURS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест через 2 месяца";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(MONTHS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void afterYears() {
        String str = "Тест через 2 года 2 месяца 2 дня 2 часа";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(YEARS, "2"), new TimeLexem(MONTHS, "2"), new TimeLexem(DAYS, "2"), new TimeLexem(HOURS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест через 2 года";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(YEARS, "2")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    private LinkedList<BaseLexem> expected(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }
}