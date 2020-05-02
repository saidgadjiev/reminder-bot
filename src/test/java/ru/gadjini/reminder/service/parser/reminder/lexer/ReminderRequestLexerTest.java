package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.pattern.Patterns;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalisationService.class, ReminderRequestLexerConfig.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class ReminderRequestLexerTest {

    private static final Locale LOCALE = new Locale("ru");

    private static final TimeLexerConfig TIME_LEXER_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        Mockito.when(TIME_LEXER_CONFIG.getTimePattern(any())).thenReturn(new GroupPattern(Patterns.FIXED_TIME_PATTERN, PatternBuilder.FIXED_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern(any())).thenReturn(new GroupPattern(Patterns.OFFSET_TIME_PATTERN, PatternBuilder.OFFSET_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatTimePattern(any())).thenReturn(new GroupPattern(Patterns.REPEAT_TIME_PATTERN, PatternBuilder.REPEAT_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatWordPattern(any())).thenReturn(new GroupPattern(Patterns.REPEAT_WORD_PATTERN, Collections.emptyList()));
    }

    @Autowired
    private ReminderRequestLexerConfig lexerConfig;

    @Test
    void fixedTime() {
        String str = "Тест 25 января 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, LOCALE);
        List<Lexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new Lexem(TimeToken.DAY, "25"), new Lexem(TimeToken.MONTH_WORD, "января"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
    }

    @Test
    void repeatTime() {
        String str = "Тест каждое 25 января 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, LOCALE);
        List<Lexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new Lexem(TimeToken.REPEAT, ""), new Lexem(TimeToken.DAY, "25"), new Lexem(TimeToken.MONTH_WORD, "января"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
    }

    @Test
    void repeatTimes() {
        String str = "Тест каждое 25 января 19:30 среду 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, LOCALE);
        List<Lexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new Lexem(TimeToken.REPEAT, ""), new Lexem(TimeToken.DAY, "25"), new Lexem(TimeToken.MONTH_WORD, "января"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30"), new Lexem(TimeToken.DAY_OF_WEEK, "среду"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
    }

    @Test
    void offsetTime() {
        String str = "Тест через 2 года 2 месяца 2 дня в 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, LOCALE);
        List<Lexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new Lexem(TimeToken.OFFSET, ""), new Lexem(TimeToken.TYPE, "через"), new Lexem(TimeToken.YEARS, "2"), new Lexem(TimeToken.MONTHS, "2"), new Lexem(TimeToken.DAYS, "2"), new Lexem(TimeToken.HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")), lexems);
    }

    private LinkedList<Lexem> expected(Lexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }
}