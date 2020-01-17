package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.parser.api.BaseLexem;

import java.util.LinkedList;
import java.util.List;

public class TimeLexer {

    private RepeatTimeLexer repeatTimeLexer;

    private OffsetTimeLexer offsetTimeLexer;

    private FixedTimeLexer fixedTimeLexer;

    private String str;

    private int end;

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str) {
        this(timeLexerConfig, str, false);
    }

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str, boolean fullMatch) {
        this.str = str;

        str = str.toLowerCase();
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, str, fullMatch);
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
