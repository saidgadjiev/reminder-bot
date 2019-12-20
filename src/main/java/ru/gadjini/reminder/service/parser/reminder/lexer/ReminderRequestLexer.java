package ru.gadjini.reminder.service.parser.reminder.lexer;

import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReminderRequestLexer {

    private final ReminderRequestLexerConfig lexerConfig;

    private TimeLexer timeLexer;

    private String[] parts;

    public ReminderRequestLexer(ReminderRequestLexerConfig lexerConfig, TimeLexerConfig timeLexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.parts = str.split(";");

        for (int i = 0; i < parts.length; ++i) {
            this.parts[i] = this.parts[i].trim();
        }
        this.timeLexer = new TimeLexer(timeLexerConfig, parts[0]);
    }

    public List<BaseLexem> tokenize() {
        LinkedList<BaseLexem> lexems = new LinkedList<>(timeLexer.tokenize());

        String tokenizeStr = parts[0].substring(0, parts[0].length() - timeLexer.end()).trim();

        return tokenizeReminderTextAndNote(tokenizeStr, lexems);
    }

    private LinkedList<BaseLexem> tokenizeReminderTextAndNote(String tokenizeStr, LinkedList<BaseLexem> lexems) {
        GroupPattern loginPattern = lexerConfig.getLoginPattern();
        GroupMatcher loginMatcher = loginPattern.matcher(tokenizeStr);

        if (loginMatcher.matches()) {
            Map<String, String> values = loginMatcher.values();

            lexems.addFirst(new ReminderLexem(ReminderToken.TEXT, values.get(PatternBuilder.TEXT).trim()));
            if (values.containsKey(PatternBuilder.LOGIN)) {
                lexems.addFirst(new ReminderLexem(ReminderToken.LOGIN, values.get(PatternBuilder.LOGIN).trim()));
            }
            if (parts.length > 1) {
                lexems.add(new ReminderLexem(ReminderToken.NOTE, parts[1]));
            }

            return lexems;
        }

        throw new ParseException();
    }
}
