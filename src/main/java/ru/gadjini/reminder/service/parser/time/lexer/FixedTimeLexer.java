package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FixedTimeLexer {

    private TimeLexerConfig lexerConfig;

    private String str;

    private int matchEnd;

    public FixedTimeLexer(TimeLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str;
    }

    public LinkedList<BaseLexem> tokenize() {
        GroupPattern pattern = lexerConfig.getTimePattern();
        GroupMatcher timeMatcher = pattern.maxMatcher(str);

        if (timeMatcher != null) {
            Map<String, String> values = timeMatcher.values();

            matchEnd = timeMatcher.end();

            return toLexems(values);
        }

        return null;
    }

    public int end() {
        return matchEnd;
    }

    private LinkedList<BaseLexem> toLexems(Map<String, String> values) {
        LinkedList<BaseLexem> lexems = new LinkedList<>();

        if (values.containsKey(PatternBuilder.DAY_WORD)) {
            lexems.add(new TimeLexem(TimeToken.DAY_WORD, values.get(PatternBuilder.DAY_WORD)));
        }
        if (values.containsKey(PatternBuilder.MONTH)) {
            lexems.add(new TimeLexem(TimeToken.MONTH, values.get(PatternBuilder.MONTH)));
        }
        if (values.containsKey(PatternBuilder.DAY)) {
            lexems.add(new TimeLexem(TimeToken.DAY, values.get(PatternBuilder.DAY)));
        }
        if (values.containsKey(PatternBuilder.MONTH_WORD)) {
            lexems.add(new TimeLexem(TimeToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
        }
        if (values.containsKey(PatternBuilder.NEXT_WEEK)) {
            lexems.add(new TimeLexem(TimeToken.NEXT_WEEK, values.get(PatternBuilder.NEXT_WEEK)));
        }
        if (values.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
            lexems.add(new TimeLexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.DAY_OF_WEEK_WORD)));
        }
        if (values.containsKey(PatternBuilder.HOUR)) {
            lexems.add(new TimeLexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
            lexems.add(new TimeLexem(TimeToken.MINUTE, values.get(PatternBuilder.MINUTE)));
        }

        return lexems;
    }
}
