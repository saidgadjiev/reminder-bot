package ru.gadjini.reminder.service.parser.remind.lexer;

import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.time.lexer.*;

import java.util.ArrayList;
import java.util.List;

public class CustomRemindLexer {

    private RepeatTimeLexer repeatTimeLexer;

    private OffsetTimeLexer offsetTimeLexer;

    private TimeLexer timeLexer;

    public CustomRemindLexer(TimeLexerConfig timeLexerConfig, String str) {
        str = str.toLowerCase();
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, str);
        this.timeLexer = new TimeLexer(timeLexerConfig, str);
        this.offsetTimeLexer = new OffsetTimeLexer(timeLexerConfig, str);
    }

    public List<BaseLexem> tokenize() {
        List<BaseLexem> lexems = offsetTimeLexer.tokenize();

        if (lexems != null) {
            return lexems;
        }

        lexems = repeatTimeLexer.tokenize();

        if (lexems != null) {
            List<BaseLexem> toReturn = new ArrayList<>();
            toReturn.add(new TimeLexem(TimeToken.REPEAT, ""));
            toReturn.addAll(lexems);

            return toReturn;
        }

        lexems = timeLexer.tokenize();

        if (lexems != null) {
            return lexems;
        }

        throw new ParseException();
    }
}
