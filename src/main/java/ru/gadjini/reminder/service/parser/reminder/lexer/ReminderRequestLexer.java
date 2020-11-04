package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ReminderRequestLexer {

    private final ReminderRequestLexerConfig lexerConfig;

    private TimeLexer timeLexer;

    private final TimeLexerConfig timeLexerConfig;

    private final Locale locale;

    private String[] parts;

    public ReminderRequestLexer(ReminderRequestLexerConfig lexerConfig, TimeLexerConfig timeLexerConfig, String str, Locale locale) {
        this.lexerConfig = lexerConfig;
        this.timeLexerConfig = timeLexerConfig;
        this.locale = locale;
        this.parts = breakToTextAndNote(str);
        this.timeLexer = new TimeLexer(timeLexerConfig, parts[0], locale);
    }

    public List<Lexem> tokenize() {
        LinkedList<Lexem> lexems = timeLexer.tokenize();

        if (lexems == null) {
            lexems = new LinkedList<>();
        }

        String tokenizeStr = timeLexer.eraseTime().trim();

        return breakRequestToParts(tokenizeStr, lexems);
    }

    private LinkedList<Lexem> breakRequestToParts(String tokenizeStr, LinkedList<Lexem> lexems) {
        lexems.addFirst(new ReminderLexem(ReminderToken.TEXT, StringUtils.capitalize(tokenizeStr.trim())));

        if (parts.length > 1) {
            if (StringUtils.isNotBlank(parts[1])) {
                lexems.add(new ReminderLexem(ReminderToken.NOTE, parts[1]));
            }
        }
        if (parts.length > 2) {
            if (StringUtils.isNotBlank(parts[2])) {
                lexems.add(new ReminderLexem(ReminderToken.ESTIMATE, ""));
                LinkedList<Lexem> estimateLexems = new TimeLexer(timeLexerConfig, parts[2], true, true, locale).tokenize();
                lexems.addAll(estimateLexems);
            }
        }

        return lexems;
    }

    private String[] breakToTextAndNote(String str) {
        String[] parts = str.split(lexerConfig.getRequestPartsBreakPattern(locale));

        for (int i = 0; i < parts.length; ++i) {
            parts[i] = parts[i].trim();
        }

        return parts;
    }
}
