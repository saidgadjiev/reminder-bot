package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.context.UserContextResolver;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.pattern.Patterns;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.util.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserContextResolver.class, LocalisationService.class, ReminderRequestLexerConfig.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class ReminderRequestLexerTest {

    private static final TimeLexerConfig TIME_LEXER_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        Mockito.when(TIME_LEXER_CONFIG.getTimePattern()).thenReturn(new GroupPattern(Patterns.FIXED_TIME_PATTERN, PatternBuilder.FIXED_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern()).thenReturn(new GroupPattern(Patterns.OFFSET_TIME_PATTERN, PatternBuilder.OFFSET_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatTimePattern()).thenReturn(new GroupPattern(Patterns.REPEAT_TIME_PATTERN, PatternBuilder.REPEAT_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getRepeatWordPattern()).thenReturn(new GroupPattern(Patterns.REPEAT_WORD_PATTERN, Collections.emptyList()));
    }

    @SpyBean
    private LocalisationService localisationService;

    @Autowired
    private ReminderRequestLexerConfig lexerConfig;

    @BeforeEach
    void setUp() {
        Mockito.doReturn(new Locale("ru")).when(localisationService).getCurrentLocale("ru");
    }

    @Test
    void fixedTime() {
        String str = "Тест 25 января 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, localisationService.getCurrentLocale("ru"));
        List<BaseLexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "января"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
    }

    @Test
    void repeatTime() {
        String str = "Тест каждое 25 января 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, localisationService.getCurrentLocale("ru"));
        List<BaseLexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(TimeToken.DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "января"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
    }

    @Test
    void repeatTimes() {
        String str = "Тест каждое 25 января 19:30 среду 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, localisationService.getCurrentLocale("ru"));
        List<BaseLexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(TimeToken.DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "января"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30"), new TimeLexem(TimeToken.DAY_OF_WEEK, "среду"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
    }

    @Test
    void offsetTime() {
        String str = "Тест через 2 года 2 месяца 2 дня в 19:30";
        ReminderRequestLexer lexer = new ReminderRequestLexer(lexerConfig, TIME_LEXER_CONFIG, str, localisationService.getCurrentLocale("ru"));
        List<BaseLexem> lexems = lexer.tokenize();
        Assert.assertEquals(expected(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.OFFSET, ""), new TimeLexem(TimeToken.TYPE, "через"), new TimeLexem(TimeToken.YEARS, "2"), new TimeLexem(TimeToken.MONTHS, "2"), new TimeLexem(TimeToken.DAYS, "2"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")), lexems);
    }

    private LinkedList<BaseLexem> expected(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }
}