package ru.gadjini.reminder.service.parser.postpone.lexer;

import org.junit.Assert;
import org.mockito.Mockito;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;

import java.util.List;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.eq;
import static ru.gadjini.reminder.service.parser.pattern.PatternBuilder.*;

class PostponeRequestLexerTest {

    private static final PostponeLexerConfig MOCK_CONFIG = Mockito.mock(PostponeLexerConfig.class);

    private static final TimeLexerConfig MOCK_TIME_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        GroupPattern typeOnPattern = new GroupPattern(Pattern.compile("((?<day>\\d+)д)?( )?((?<hour>\\d+)ч)?( )?((?<minute>\\d+)мин)?"), List.of(DAY, HOUR, MINUTE));
        Mockito.when(MOCK_CONFIG.getPattern(eq(ParsedPostponeTime.Type.ON))).thenReturn(typeOnPattern);

        GroupPattern typeAtPattern = new GroupPattern(
                Pattern.compile("((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9])) (в )?((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )?(((?<month>1[0-2]|[1-9])\\.)?(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>завтра|послезавтра))?"),
                List.of(HOUR, MINUTE, MONTH_WORD, MONTH, DAY, DAY_WORD)
        );
        Mockito.when(MOCK_CONFIG.getPattern(eq(ParsedPostponeTime.Type.AT))).thenReturn(typeAtPattern);
    }

    @org.junit.jupiter.api.Test
    void tokenizeOnHour() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(MOCK_CONFIG, MOCK_TIME_CONFIG, "на 1ч");

        List<BaseLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 2);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ON_HOUR, "1");
    }

    @org.junit.jupiter.api.Test
    void tokenizeOnDay() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(MOCK_CONFIG, MOCK_TIME_CONFIG, "на 1д");

        List<BaseLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 2);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ON_DAY, "1");
    }

    @org.junit.jupiter.api.Test
    void tokenizeOnMinute() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(MOCK_CONFIG, MOCK_TIME_CONFIG, "на 1мин");

        List<BaseLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 2);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ON_MINUTE, "1");
    }

    @org.junit.jupiter.api.Test
    void tokenizeOnCombinations() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(MOCK_CONFIG, MOCK_TIME_CONFIG, "на 1д 1ч 1мин");

        List<BaseLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 4);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ON_DAY, "1");
        assertLexem(lexems.get(2), PostponeToken.ON_HOUR, "1");
        assertLexem(lexems.get(3), PostponeToken.ON_MINUTE, "1");


        lexer = new PostponeRequestLexer(MOCK_CONFIG, MOCK_TIME_CONFIG, "на 1д 1мин");

        lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 3);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ON_DAY, "1");
        assertLexem(lexems.get(2), PostponeToken.ON_MINUTE, "1");

        lexer = new PostponeRequestLexer(MOCK_CONFIG, MOCK_TIME_CONFIG, "на 1д 1ч");

        lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 3);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ON_DAY, "1");
        assertLexem(lexems.get(2), PostponeToken.ON_HOUR, "1");

        lexer = new PostponeRequestLexer(MOCK_CONFIG, MOCK_TIME_CONFIG, "на 1ч 1мин");

        lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 3);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ON_HOUR, "1");
        assertLexem(lexems.get(2), PostponeToken.ON_MINUTE, "1");
    }

    private void assertLexem(BaseLexem lexem, PostponeToken token, String value) {
        Assert.assertEquals(lexem.getToken(), token);
        Assert.assertEquals(lexem.getValue(), value);
    }
}