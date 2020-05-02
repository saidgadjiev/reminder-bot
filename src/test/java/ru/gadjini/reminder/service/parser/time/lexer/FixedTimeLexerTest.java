package ru.gadjini.reminder.service.parser.time.lexer;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.pattern.Patterns;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.DAY;

class FixedTimeLexerTest {

    private static final Locale LOCALE = new Locale("ru");

    private static final TimeLexerConfig TIME_LEXER_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        Mockito.when(TIME_LEXER_CONFIG.getTimePattern(any())).thenReturn(new GroupPattern(Patterns.FIXED_TIME_PATTERN, PatternBuilder.FIXED_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern(any())).thenReturn(new GroupPattern(Patterns.OFFSET_TIME_PATTERN, PatternBuilder.OFFSET_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatTimePattern(any())).thenReturn(new GroupPattern(Patterns.REPEAT_TIME_PATTERN, PatternBuilder.REPEAT_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatWordPattern(any())).thenReturn(new GroupPattern(Patterns.REPEAT_WORD_PATTERN, Collections.emptyList()));
    }

    @Test
    void hourMinute() {
        String str = "Тест в 19";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<Lexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "00")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест в 19:30";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void tomorrow() {
        dayWordTest("завтра");
    }

    @Test
    void dayAfterTomorrow() {
        dayWordTest("послезавтра");
    }

    @Test
    void dayOfWeek() {
        String str = "Тест во вторник в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<Lexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.DAY_OF_WEEK, "вторник"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест в пятницу";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.DAY_OF_WEEK, "пятницу")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void nextDayOfWeek() {
        String str = "Тест в след вторник в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<Lexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.NEXT_WEEK, "след"), new Lexem(TimeToken.DAY_OF_WEEK, "вторник"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест в след воскресенье";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.NEXT_WEEK, "след"), new Lexem(TimeToken.DAY_OF_WEEK, "воскресенье")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void dayOfMonth() {
        String str = "Тест 25 сентября в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<Lexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(DAY, "25"), new Lexem(TimeToken.MONTH_WORD, "сентября"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест 25 января";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(DAY, "25"), new Lexem(TimeToken.MONTH_WORD, "января")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void yearMonthDay() {
        String str = "Тест 2030.01.05 в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<Lexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.YEAR, "2030"), new Lexem(TimeToken.MONTH, "01"), new Lexem(DAY, "05"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест 2030.01.05";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.YEAR, "2030"), new Lexem(TimeToken.MONTH, "01"), new Lexem(DAY, "05")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void monthDay() {
        String str = "Тест 01.05 в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<Lexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.MONTH, "01"), new Lexem(DAY, "05"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест 01.05";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.MONTH, "01"), new Lexem(DAY, "05")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    private void dayWordTest(String dayWord) {
        String str = "Тест " + dayWord + " в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        LinkedList<Lexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.DAY_WORD, dayWord), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест " + dayWord;
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str, LOCALE);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new Lexem(TimeToken.DAY_WORD, dayWord)), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    private LinkedList<Lexem> expected(Lexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }
}