package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.time.lexer.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//каждые 5мин
//каждое воскресенье в 19:00
//каждый вторник в 19:00
//каждый день в 19:00
public class ReminderRequestLexer {

    private final ReminderRequestLexerConfig lexerConfig;

    private RepeatTimeLexer repeatTimeLexer;

    private TimeLexer timeLexer;

    private String[] parts;

    public ReminderRequestLexer(ReminderRequestLexerConfig lexerConfig, TimeLexerConfig timeLexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.parts = str.split(";");

        for (int i = 0; i < parts.length; ++i) {
            this.parts[i] = this.parts[i].trim();
        }
        this.timeLexer = new TimeLexer(timeLexerConfig, parts[0]);
        this.repeatTimeLexer = new RepeatTimeLexer(timeLexerConfig, parts[0]);
    }

    public List<BaseLexem> tokenize() {
        List<BaseLexem> lexems = tokenizeRepeatRequest();

        if (lexems != null) {
            return lexems;
        }

        lexems = tokenizeStandardRequest();

        if (lexems != null) {
            return lexems;
        }

        throw new ParseException();
    }

    public List<BaseLexem> tokenizeTime() {
        LinkedList<BaseLexem> lexems = new LinkedList<>(timeLexer.tokenize());

        if (lexems.size() > 0) {
            return lexems;
        }

        throw new ParseException();
    }

    private LinkedList<BaseLexem> tokenizeRepeatRequest() {
        List<BaseLexem> timeLexems = repeatTimeLexer.tokenize();

        if (timeLexems == null) {
            return null;
        }
        LinkedList<BaseLexem> lexems = new LinkedList<>();
        lexems.add(new TimeLexem(TimeToken.REPEAT, ""));
        lexems.addAll(timeLexems);

        String tokenizeStr = parts[0].substring(0, parts[0].length() - repeatTimeLexer.end()).trim();

        return tokenizeReminderTextAndNote(tokenizeStr, lexems);
    }

    private LinkedList<BaseLexem> tokenizeStandardRequest() {
        List<BaseLexem> timeLexems = timeLexer.tokenize();

        if (timeLexems == null) {
            return null;
        }
        LinkedList<BaseLexem> lexems = new LinkedList<>(timeLexems);

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

        return null;
    }
}
