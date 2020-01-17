package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.Map;

public class RepeatTimeLexer {

    private TimeLexerConfig lexerConfig;

    private String str;

    private int matchEnd;

    public RepeatTimeLexer(TimeLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str;
    }

    public LinkedList<BaseLexem> tokenize() {
        LinkedList<BaseLexem> lexems = new LinkedList<>();

        GroupMatcher repeatTimeMatcher = lexerConfig.getRepeatTimePattern().maxMatcher(str);

        if (repeatTimeMatcher != null) {
            Map<String, String> values = repeatTimeMatcher.values();

            if (values.containsKey(PatternBuilder.SUFFIX_HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.SUFFIX_HOURS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.PREFIX_HOURS)));
            }
            if (values.containsKey(PatternBuilder.EVERY_HOUR)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_HOUR, values.get(PatternBuilder.EVERY_HOUR)));
            }
            if (values.containsKey(PatternBuilder.SUFFIX_MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.SUFFIX_MINUTES)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.PREFIX_MINUTES)));
            }
            if (values.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
                lexems.add(new TimeLexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.DAY_OF_WEEK_WORD)));
            }
            if (values.containsKey(PatternBuilder.EVERY_DAY)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_DAY, values.get(PatternBuilder.EVERY_DAY)));
            }
            if (values.containsKey(PatternBuilder.EVERY_MINUTE)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_MINUTE, values.get(PatternBuilder.EVERY_MINUTE)));
            }
            if (values.containsKey(PatternBuilder.EVERY_YEAR)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_YEAR, values.get(PatternBuilder.EVERY_YEAR)));
            }
            if (values.containsKey(PatternBuilder.DAY)) {
                lexems.add(new TimeLexem(TimeToken.DAY, values.get(PatternBuilder.DAY)));
            }
            if (values.containsKey(PatternBuilder.MONTH_WORD)) {
                lexems.add(new TimeLexem(TimeToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
            }
            if (values.containsKey(PatternBuilder.MONTHS)) {
                lexems.add(new TimeLexem(TimeToken.MONTHS, values.get(PatternBuilder.MONTHS)));
            }
            if (values.containsKey(PatternBuilder.EVERY_MONTH)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_MONTH, values.get(PatternBuilder.EVERY_MONTH)));
            }
            if (values.containsKey(PatternBuilder.EVERY_MONTH_DAY)) {
                lexems.add(new TimeLexem(TimeToken.DAY, values.get(PatternBuilder.EVERY_MONTH_DAY)));
            }
            if (values.containsKey(PatternBuilder.SUFFIX_DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.SUFFIX_DAYS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.PREFIX_DAYS)));
            }
            if (values.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new TimeLexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
            }
            if (values.containsKey(PatternBuilder.MINUTE)) {
                lexems.add(new TimeLexem(TimeToken.MINUTE, values.get(PatternBuilder.MINUTE)));
            }
            matchEnd = repeatTimeMatcher.end();

            return lexems;
        }

        return null;
    }

    public int end() {
        return matchEnd;
    }
}
