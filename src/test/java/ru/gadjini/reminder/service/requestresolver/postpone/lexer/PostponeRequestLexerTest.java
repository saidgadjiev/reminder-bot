package ru.gadjini.reminder.service.requestresolver.postpone.lexer;

import org.junit.Assert;
import ru.gadjini.reminder.service.requestresolver.postpone.parser.ParsedPostponeTime;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class PostponeRequestLexerTest {

    private final Map<ParsedPostponeTime.Type, Pattern> patterns = Map.ofEntries(
            Map.entry(ParsedPostponeTime.Type.ON, Pattern.compile("((?<onday>\\d+)д)?( )?((?<onhour>\\d+)ч)?( )?((?<onminute>\\d+)мин)?")),
            Map.entry(ParsedPostponeTime.Type.AT, Pattern.compile("((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9])) (в )?((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )?(((?<month>1[0-2]|[1-9])\\.)?(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>завтра|послезавтра))?"))
    );

    @org.junit.jupiter.api.Test
    void tokenizeOnHour() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(patterns, "на 1ч");

        List<PostponeLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 2);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ONHOUR, "1");
    }

    @org.junit.jupiter.api.Test
    void tokenizeOnDay() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(patterns, "на 1д");

        List<PostponeLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 2);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ONDAY, "1");
    }

    @org.junit.jupiter.api.Test
    void tokenizeOnMinute() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(patterns, "на 1мин");

        List<PostponeLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 2);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ONMINUTE, "1");
    }

    @org.junit.jupiter.api.Test
    void tokenizeOnCombinations() {
        PostponeRequestLexer lexer = new PostponeRequestLexer(patterns, "на 1д 1ч 1мин");

        List<PostponeLexem> lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 4);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ONDAY, "1");
        assertLexem(lexems.get(2), PostponeToken.ONHOUR, "1");
        assertLexem(lexems.get(3), PostponeToken.ONMINUTE, "1");


        lexer = new PostponeRequestLexer(patterns, "на 1д 1мин");

        lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 3);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ONDAY, "1");
        assertLexem(lexems.get(2), PostponeToken.ONMINUTE, "1");

        lexer = new PostponeRequestLexer(patterns, "на 1д 1ч");

        lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 3);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ONDAY, "1");
        assertLexem(lexems.get(2), PostponeToken.ONHOUR, "1");

        lexer = new PostponeRequestLexer(patterns, "на 1ч 1мин");

        lexems = lexer.tokenize();

        Assert.assertEquals(lexems.size(), 3);
        assertLexem(lexems.get(0), PostponeToken.TYPE, "на");
        assertLexem(lexems.get(1), PostponeToken.ONHOUR, "1");
        assertLexem(lexems.get(2), PostponeToken.ONMINUTE, "1");
    }

    private void assertLexem(PostponeLexem lexem, PostponeToken token, String value) {
        Assert.assertEquals(lexem.getToken(), token);
        Assert.assertEquals(lexem.getValue(), value);
    }
}