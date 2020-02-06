package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.parser.api.BaseLexem;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TimeLexer {

    private RepeatTimeLexer repeatTimeLexer;

    private OffsetTimeLexer offsetTimeLexer;

    private FixedTimeLexer fixedTimeLexer;

    private String str;

    private int end;

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str, Locale locale) {
        this(timeLexerConfig, str, false, locale);
    }

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str, boolean fullMatch, Locale locale) {
        this.str = str;

        str = str.toLowerCase();
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, str, fullMatch, locale);
        this.fixedTimeLexer = new FixedTimeLexer(timeLexerConfig, str, fullMatch);
        this.offsetTimeLexer = new OffsetTimeLexer(timeLexerConfig, str, fullMatch);
    }

    public List<BaseLexem> tokenizeThrowParseException() {
        List<BaseLexem> lexems = tokenize();

        if (lexems == null) {
            throw new ParseException();
        }

        return lexems;
    }

    public LinkedList<BaseLexem> tokenize() {
        LinkedList<BaseLexem> lexems = offsetTimeLexer.tokenize();

        if (lexems != null) {
            lexems.addFirst(new TimeLexem(TimeToken.OFFSET, ""));
            end = offsetTimeLexer.end();
            return lexems;
        }

        lexems = repeatTimeLexer.tokenize();

        if (lexems != null) {
            end = repeatTimeLexer.end();
            lexems.addFirst(new TimeLexem(TimeToken.REPEAT, ""));
            return lexems;
        }

        lexems = fixedTimeLexer.tokenize();

        if (lexems != null) {
            end = fixedTimeLexer.end();
            return lexems;
        }

        return null;
    }

    public String eraseTime() {
        return str.substring(0, str.length() - end).trim();
    }
}
