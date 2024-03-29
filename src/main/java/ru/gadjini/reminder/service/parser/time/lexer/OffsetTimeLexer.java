package ru.gadjini.reminder.service.parser.time.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

public class OffsetTimeLexer {

    private TimeLexerConfig lexerConfig;

    private final String str;

    private final boolean fullMatch;

    private final Locale locale;

    private int end;

    public OffsetTimeLexer(TimeLexerConfig lexerConfig, String str, boolean fullMatch, Locale locale) {
        this.lexerConfig = lexerConfig;
        this.str = str;
        this.fullMatch = fullMatch;
        this.locale = locale;
    }

    @SuppressWarnings("CPD-START")
    public LinkedList<Lexem> tokenize() {
        GroupMatcher matcher = matcher();

        if (matcher != null) {
            Map<String, String> values = matcher.values();
            LinkedList<Lexem> lexems = new LinkedList<>();

            if (values.containsKey(PatternBuilder.TYPE)) {
                lexems.add(new Lexem(TimeToken.TYPE, values.get(PatternBuilder.TYPE)));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_YEARS)) {
                lexems.add(new Lexem(TimeToken.YEARS, values.get(PatternBuilder.SUFFIX_YEARS)));
            } else if (values.containsKey(PatternBuilder.PREFIX_YEARS)) {
                lexems.add(new Lexem(TimeToken.YEARS, values.get(PatternBuilder.PREFIX_YEARS)));
            } else if (values.containsKey(PatternBuilder.ONE_YEAR)) {
                lexems.add(new Lexem(TimeToken.YEARS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_MONTHS)) {
                lexems.add(new Lexem(TimeToken.MONTHS, values.get(PatternBuilder.SUFFIX_MONTHS)));
            } else if (values.containsKey(PatternBuilder.PREFIX_MONTHS)) {
                lexems.add(new Lexem(TimeToken.MONTHS, values.get(PatternBuilder.PREFIX_MONTHS)));
            } else if (values.containsKey(PatternBuilder.ONE_MONTH)) {
                lexems.add(new Lexem(TimeToken.MONTHS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_WEEKS)) {
                lexems.add(new Lexem(TimeToken.WEEKS, values.get(PatternBuilder.SUFFIX_WEEKS)));
            } else if (values.containsKey(PatternBuilder.PREFIX_WEEKS)) {
                lexems.add(new Lexem(TimeToken.WEEKS, values.get(PatternBuilder.PREFIX_WEEKS)));
            } else if (values.containsKey(PatternBuilder.ONE_WEEK)) {
                lexems.add(new Lexem(TimeToken.WEEKS, "1"));
            }

            if (values.containsKey(PatternBuilder.WEEKS_DAY_OF_WEEK_WORD)) {
                lexems.add(new Lexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.WEEKS_DAY_OF_WEEK_WORD)));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_DAYS)) {
                lexems.add(new Lexem(TimeToken.DAYS, values.get(PatternBuilder.SUFFIX_DAYS)));
            } else if (values.containsKey(PatternBuilder.PREFIX_DAYS)) {
                lexems.add(new Lexem(TimeToken.DAYS, values.get(PatternBuilder.PREFIX_DAYS)));
            } else if (values.containsKey(PatternBuilder.ONE_DAY)) {
                lexems.add(new Lexem(TimeToken.DAYS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_HOURS)) {
                lexems.add(new Lexem(TimeToken.HOURS, values.get(PatternBuilder.SUFFIX_HOURS)));
            } else if (values.containsKey(PatternBuilder.PREFIX_HOURS)) {
                lexems.add(new Lexem(TimeToken.HOURS, values.get(PatternBuilder.PREFIX_HOURS)));
            } else if (values.containsKey(PatternBuilder.ONE_HOUR)) {
                lexems.add(new Lexem(TimeToken.HOURS, "1"));
            }

            if (values.containsKey(PatternBuilder.SUFFIX_MINUTES)) {
                lexems.add(new Lexem(TimeToken.MINUTES, values.get(PatternBuilder.SUFFIX_MINUTES)));
            } else if (values.containsKey(PatternBuilder.PREFIX_MINUTES)) {
                lexems.add(new Lexem(TimeToken.MINUTES, values.get(PatternBuilder.PREFIX_MINUTES)));
            } else if (values.containsKey(PatternBuilder.ONE_MINUTE)) {
                lexems.add(new Lexem(TimeToken.MINUTES, "1"));
            }

            if (values.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new Lexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
                lexems.add(new Lexem(TimeToken.MINUTE, values.getOrDefault(PatternBuilder.MINUTE, "00")));
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
