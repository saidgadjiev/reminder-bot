package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindLexem;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OffsetTimeLexer {

    private TimeLexerConfig lexerConfig;

    private final String str;

    private int end;

    public OffsetTimeLexer(TimeLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str;
    }

    public List<BaseLexem> tokenize() {
        GroupPattern pattern = lexerConfig.getOffsetTimePattern();
        GroupMatcher matcher = pattern.maxMatcher(str);

        if (matcher != null) {
            Map<String, String> values = matcher.values();
            List<BaseLexem> lexems = new ArrayList<>();

            if (values.containsKey(PatternBuilder.TYPE)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.TYPE, values.get(PatternBuilder.TYPE)));
            }
            if (values.containsKey(PatternBuilder.DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, values.get(PatternBuilder.DAYS)));
            }
            if (values.containsKey(PatternBuilder.EVE)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, "1"));
            }
            if (values.containsKey(PatternBuilder.HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, values.get(PatternBuilder.HOURS)));
            }
            if (values.containsKey(PatternBuilder.MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, values.get(PatternBuilder.MINUTES)));
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
