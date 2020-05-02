package ru.gadjini.reminder.service.parser.time.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

public class FixedTimeLexer {

    private TimeLexerConfig lexerConfig;

    private String str;

    private int matchEnd;

    private boolean fullMatch;

    private final Locale locale;

    public FixedTimeLexer(TimeLexerConfig lexerConfig, String str, boolean fullMatch, Locale locale) {
        this.lexerConfig = lexerConfig;
        this.str = str;
        this.fullMatch = fullMatch;
        this.locale = locale;
    }

    public LinkedList<Lexem> tokenize() {
        GroupMatcher timeMatcher = matcher();

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

    private LinkedList<Lexem> toLexems(Map<String, String> values) {
        LinkedList<Lexem> lexems = new LinkedList<>();

        if (values.containsKey(PatternBuilder.TYPE)) {
            lexems.add(new Lexem(TimeToken.TYPE, values.get(PatternBuilder.TYPE)));
        }
        if (values.containsKey(PatternBuilder.DAY_WORD)) {
            lexems.add(new Lexem(TimeToken.DAY_WORD, values.get(PatternBuilder.DAY_WORD)));
        }
        if (values.containsKey(PatternBuilder.YEAR)) {
            lexems.add(new Lexem(TimeToken.YEAR, values.get(PatternBuilder.YEAR)));
        }
        if (values.containsKey(PatternBuilder.MONTH)) {
            lexems.add(new Lexem(TimeToken.MONTH, values.get(PatternBuilder.MONTH)));
        }
        if (values.containsKey(PatternBuilder.DAY)) {
            lexems.add(new Lexem(TimeToken.DAY, values.get(PatternBuilder.DAY)));
        }
        if (values.containsKey(PatternBuilder.MONTH_WORD)) {
            lexems.add(new Lexem(TimeToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
        }
        if (values.containsKey(PatternBuilder.NEXT_WEEK)) {
            lexems.add(new Lexem(TimeToken.NEXT_WEEK, values.get(PatternBuilder.NEXT_WEEK)));
        }
        if (values.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
            lexems.add(new Lexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.DAY_OF_WEEK_WORD)));
        }
        if (values.containsKey(PatternBuilder.HOUR)) {
            lexems.add(new Lexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
            lexems.add(new Lexem(TimeToken.MINUTE, values.getOrDefault(PatternBuilder.MINUTE, "00")));
        }

        return lexems;
    }

    private GroupMatcher matcher() {
        if (fullMatch) {
            GroupMatcher matcher = lexerConfig.getTimePattern(locale).matcher(StringUtils.reverseDelimited(str, ' '));

            if (matcher.matches()) {
                return matcher;
            }

            return null;
        }

        return lexerConfig.getTimePattern(locale).maxMatcher(str);
    }
}
