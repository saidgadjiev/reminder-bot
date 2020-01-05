package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;

import java.util.LinkedList;
import java.util.List;

public class ReminderRequestLexer {

    private final ReminderRequestLexerConfig lexerConfig;

    private TimeLexer timeLexer;

    private String[] parts;

    public ReminderRequestLexer(ReminderRequestLexerConfig lexerConfig, TimeLexerConfig timeLexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.parts = breakToTextAndNote(str);
        this.timeLexer = new TimeLexer(timeLexerConfig, parts[0]);
    }

    public List<BaseLexem> tokenize() {
        LinkedList<BaseLexem> lexems = timeLexer.tokenize();

        if (lexems == null) {
            lexems = new LinkedList<>();
        }

        String tokenizeStr = timeLexer.eraseTime().trim();

        return tokenizeReminderTextAndNote(tokenizeStr, lexems);
    }

    private LinkedList<BaseLexem> tokenizeReminderTextAndNote(String tokenizeStr, LinkedList<BaseLexem> lexems) {
        lexems.addFirst(new ReminderLexem(ReminderToken.TEXT, StringUtils.capitalize(removeHtmlTags(tokenizeStr).trim())));

        if (parts.length > 1) {
            lexems.add(new ReminderLexem(ReminderToken.NOTE, parts[1]));
        }

        return lexems;
    }

    private String[] breakToTextAndNote(String str) {
        String[] parts = str.split(lexerConfig.getTextAndNoteBreakPattern());

        for (int i = 0; i < parts.length; ++i) {
            parts[i] = parts[i].trim();
        }

        return parts;
    }

    private String removeHtmlTags(String str) {
        return str.replaceAll("<.*?>", "");
    }
}
