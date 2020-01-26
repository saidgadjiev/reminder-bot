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

import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.DAY;

class FixedTimeLexerTest {

    private static final TimeLexerConfig TIME_LEXER_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        Mockito.when(TIME_LEXER_CONFIG.getTimePattern()).thenReturn(new GroupPattern(Patterns.FIXED_TIME_PATTERN, PatternBuilder.FIXED_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern()).thenReturn(new GroupPattern(Patterns.OFFSET_TIME_PATTERN, PatternBuilder.OFFSET_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatTimePattern()).thenReturn(new GroupPattern(Patterns.REPEAT_TIME_PATTERN, PatternBuilder.REPEAT_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatWordPattern()).thenReturn(new GroupPattern(Patterns.REPEAT_WORD_PATTERN, Collections.emptyList()));
    }

    @Test
    void hourMinute() {
        String str = "Тест в 19";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "00")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест в 19:30";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
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
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.DAY_OF_WEEK, "вторник"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест в пятницу";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.DAY_OF_WEEK, "пятницу")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void nextDayOfWeek() {
        String str = "Тест в след вторник в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.NEXT_WEEK, "след"), new TimeLexem(TimeToken.DAY_OF_WEEK, "вторник"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест в след воскресенье";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.NEXT_WEEK, "след"), new TimeLexem(TimeToken.DAY_OF_WEEK, "воскресенье")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void dayOfMonth() {
        String str = "Тест 25 сентября в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "сентября"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест 25 января";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "января")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void yearMonthDay() {
        String str = "Тест 2030.01.05 в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.YEAR, "2030"), new TimeLexem(TimeToken.MONTH, "01"), new TimeLexem(DAY, "05"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест 2030.01.05";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.YEAR, "2030"), new TimeLexem(TimeToken.MONTH, "01"), new TimeLexem(DAY, "05")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    @Test
    void monthDay() {
        String str = "Тест 01.05 в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.MONTH, "01"), new TimeLexem(DAY, "05"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест 01.05";
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.MONTH, "01"), new TimeLexem(DAY, "05")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    private void dayWordTest(String dayWord) {
        String str = "Тест " + dayWord + " в 19:30";
        TimeLexer timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.DAY_WORD, dayWord), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());

        str = "Тест " + dayWord;
        timeLexer = new TimeLexer(TIME_LEXER_CONFIG, str);
        lexems = timeLexer.tokenize();
        Assert.assertEquals(expected(new TimeLexem(TimeToken.DAY_WORD, dayWord)), lexems);
        Assert.assertEquals("Тест", timeLexer.eraseTime());
    }

    private LinkedList<BaseLexem> expected(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }
}