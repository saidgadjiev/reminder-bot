package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.Map;

public class OffsetTimeLexer {

    private TimeLexerConfig lexerConfig;

    private final String str;

    private int end;

    public OffsetTimeLexer(TimeLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str;
    }

    public LinkedList<BaseLexem> tokenize() {
        GroupPattern pattern = lexerConfig.getOffsetTimePattern();
        GroupMatcher matcher = pattern.maxMatcher(str);

        if (matcher != null) {
            Map<String, String> values = matcher.values();
            LinkedList<BaseLexem> lexems = new LinkedList<>();

            if (values.containsKey(PatternBuilder.TYPE)) {
                lexems.add(new TimeLexem(TimeToken.TYPE, values.get(PatternBuilder.TYPE)));
            }
            if (values.containsKey(PatternBuilder.SUFFIX_DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.SUFFIX_DAYS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.PREFIX_DAYS)));
            }
            if (values.containsKey(PatternBuilder.SUFFIX_HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.SUFFIX_HOURS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.PREFIX_HOURS)));
            }
            if (values.containsKey(PatternBuilder.SUFFIX_MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.SUFFIX_MINUTES)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.PREFIX_MINUTES)));
            }
            if (values.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new TimeLexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
                lexems.add(new TimeLexem(TimeToken.MINUTE, values.get(PatternBuilder.MINUTE)));
            }
            end = matcher.end();

            return lexems;
        }

        return null;
    }

    public int end() {
        return end;
    }
}
