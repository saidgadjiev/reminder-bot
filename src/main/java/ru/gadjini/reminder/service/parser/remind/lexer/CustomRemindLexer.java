package ru.gadjini.reminder.service.parser.remind.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.time.lexer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomRemindLexer {

    private CustomRemindLexerConfig lexerConfig;

    private RepeatTimeLexer repeatTimeLexer;

    private TimeLexer timeLexer;

    private final String str;

    public CustomRemindLexer(CustomRemindLexerConfig lexerConfig, TimeLexerConfig timeLexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str.toLowerCase();

        String reversed = StringUtils.reverseDelimited(this.str, ' ');
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, reversed);
        this.timeLexer = new TimeLexer(timeLexerConfig, reversed);
    }

    public List<BaseLexem> tokenize() {
        List<BaseLexem> lexems = tokenizeOffsetTime();

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

    private List<BaseLexem> tokenizeOffsetTime() {
        GroupPattern pattern = lexerConfig.getPattern();
        GroupMatcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            Map<String, String> values = matcher.values();
            List<BaseLexem> lexems = new ArrayList<>();

            if (values.containsKey(PatternBuilder.TYPE)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.TYPE, values.get(PatternBuilder.TYPE)));
            }
            if (values.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.HOUR, values.get(PatternBuilder.HOUR)));
            }
            if (values.containsKey(PatternBuilder.MINUTE)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.MINUTE, values.get(PatternBuilder.MINUTE)));
            }

            return lexems;
        }

        return null;
    }
}
