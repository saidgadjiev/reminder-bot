package ru.gadjini.reminder.service.parser.remind.lexer;

import ru.gadjini.reminder.pattern.GroupMatcher;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                lexems.add(new CustomRemindLexem(CustomRemindToken.TYPE, values.get(PatternBuilder.TYPE)));
            }
            if (values.containsKey(PatternBuilder.TTYPE)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.TTYPE, values.get(PatternBuilder.TTYPE)));
            }
            if (values.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.HOUR, values.get(PatternBuilder.HOUR)));
            }
            if (values.containsKey(PatternBuilder.MINUTE)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.MINUTE, values.get(PatternBuilder.MINUTE)));
            }
            if (values.containsKey(PatternBuilder.THOUR)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.THOUR, values.get(PatternBuilder.THOUR)));
            }
            if (values.containsKey(PatternBuilder.TMINUTE)) {
                lexems.add(new CustomRemindLexem(CustomRemindToken.TMINUTE, values.get(PatternBuilder.TMINUTE)));
            }

            return lexems;
        }

        throw new ParseException();
    }
}
