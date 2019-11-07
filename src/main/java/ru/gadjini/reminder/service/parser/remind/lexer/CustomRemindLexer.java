package ru.gadjini.reminder.service.parser.remind.lexer;

import ru.gadjini.reminder.pattern.GroupMatcher;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CustomRemindLexer {

    private CustomRemindLexerConfig lexerConfig;

    private final String str;

    public CustomRemindLexer(CustomRemindLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str.toLowerCase();
    }

    public List<CustomRemindLexem> tokenize() {
        GroupPattern pattern = lexerConfig.getPattern();
        GroupMatcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            Map<String, String> values = matcher.values();
            List<CustomRemindLexem> lexems = new ArrayList<>();

            if (values.containsKey(PatternBuilder.TYPE)) {
                lexems.add(new CustomRemindLexem(CustomReminderToken.TYPE, values.get(PatternBuilder.TYPE)));
            }
            if (values.containsKey(PatternBuilder.TTYPE)) {
                lexems.add(new CustomRemindLexem(CustomReminderToken.TTYPE, values.get(PatternBuilder.TTYPE)));
            }
            if (values.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new CustomRemindLexem(CustomReminderToken.HOUR, values.get(PatternBuilder.HOUR)));
            }
            if (values.containsKey(PatternBuilder.MINUTE)) {
                lexems.add(new CustomRemindLexem(CustomReminderToken.MINUTE, values.get(PatternBuilder.MINUTE)));
            }
            if (values.containsKey(PatternBuilder.THOUR)) {
                lexems.add(new CustomRemindLexem(CustomReminderToken.THOUR, values.get(PatternBuilder.THOUR)));
            }
            if (values.containsKey(PatternBuilder.TMINUTE)) {
                lexems.add(new CustomRemindLexem(CustomReminderToken.TMINUTE, values.get(PatternBuilder.TMINUTE)));
            }

            return lexems;
        }

        throw new ParseException();
    }
}
