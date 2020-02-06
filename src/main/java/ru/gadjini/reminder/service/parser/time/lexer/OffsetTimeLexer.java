package ru.gadjini.reminder.service.parser.time.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.Map;

public class OffsetTimeLexer {

    private TimeLexerConfig lexerConfig;

    private final String str;

    private final boolean fullMatch;

    private int end;

    public OffsetTimeLexer(TimeLexerConfig lexerConfig, String str, boolean fullMatch) {
        this.lexerConfig = lexerConfig;
        this.str = str;
        this.fullMatch = fullMatch;
    }

    @SuppressWarnings("CPD-START")
    public LinkedList<BaseLexem> tokenize() {
        GroupMatcher matcher = matcher();

        if (matcher != null) {
            Map<String, String> values = matcher.values();
            LinkedList<BaseLexem> lexems = new LinkedList<>();

            if (values.containsKey(PatternBuilder.TYPE)) {
                lexems.add(new TimeLexem(TimeToken.TYPE, values.get(PatternBuilder.TYPE)));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_YEARS)) {
                lexems.add(new TimeLexem(TimeToken.YEARS, values.get(PatternBuilder.SUFFIX_YEARS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_YEARS)) {
                lexems.add(new TimeLexem(TimeToken.YEARS, values.get(PatternBuilder.PREFIX_YEARS)));
            }
            if (values.containsKey(PatternBuilder.ONE_YEAR)) {
                lexems.add(new TimeLexem(TimeToken.YEARS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_MONTHS)) {
                lexems.add(new TimeLexem(TimeToken.MONTHS, values.get(PatternBuilder.SUFFIX_MONTHS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_MONTHS)) {
                lexems.add(new TimeLexem(TimeToken.MONTHS, values.get(PatternBuilder.PREFIX_MONTHS)));
            }
            if (values.containsKey(PatternBuilder.ONE_MONTH)) {
                lexems.add(new TimeLexem(TimeToken.MONTHS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.SUFFIX_DAYS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.PREFIX_DAYS)));
            }
            if (values.containsKey(PatternBuilder.ONE_DAY)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.SUFFIX_HOURS)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.PREFIX_HOURS)));
            }
            if (values.containsKey(PatternBuilder.ONE_HOUR)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.SUFFIX_MINUTES)));
            }
            if (values.containsKey(PatternBuilder.PREFIX_MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.PREFIX_MINUTES)));
            }

            if (values.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new TimeLexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
                lexems.add(new TimeLexem(TimeToken.MINUTE, values.getOrDefault(PatternBuilder.MINUTE, "00")));
            }
            end = matcher.end();

            return lexems;
        }

        return null;
    }

    public int end() {
        return end;
    }

    private GroupMatcher matcher() {
        if (fullMatch) {
            GroupMatcher matcher = lexerConfig.getOffsetTimePattern(locale).matcher(StringUtils.reverseDelimited(str, ' '));

            if (matcher.matches()) {
                return matcher;
            }

            return null;
        }

        return lexerConfig.getOffsetTimePattern(locale).maxMatcher(str);
    }
}
