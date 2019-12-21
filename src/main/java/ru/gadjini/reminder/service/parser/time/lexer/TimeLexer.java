package ru.gadjini.reminder.service.parser.time.lexer;

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

    private int end;

    public TimeLexer(TimeLexerConfig timeLexerConfig, String str) {
        str = str.toLowerCase();
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, str);
        this.fixedTimeLexer = new FixedTimeLexer(timeLexerConfig, str);
        this.offsetTimeLexer = new OffsetTimeLexer(timeLexerConfig, str);
    }

    public List<BaseLexem> tokenize() {
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

        throw new ParseException();
    }

    public int end() {
        return end;
    }
}
