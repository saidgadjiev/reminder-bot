package ru.gadjini.reminder.service.parser.time.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.time.lexer.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TimeLexer {

    private RepeatTimeLexer repeatTimeLexer;

    private OffsetTimeLexer offsetTimeLexer;

    private FixedTimeLexer fixedTimeLexer;

    private String str;

    private int end;

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str) {
        this.str = StringUtils.reverseDelimited(str.toLowerCase(), ' ');
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, this.str);
        this.fixedTimeLexer = new FixedTimeLexer(timeLexerConfig, this.str);
        this.offsetTimeLexer = new OffsetTimeLexer(timeLexerConfig, this.str);
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
        return StringUtils.reverseDelimited(str.substring(end), ' ');
    }
}
