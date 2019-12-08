package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.apache.commons.lang3.StringUtils;
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

//каждые 5мин
//каждое воскресенье в 19:00
//каждый вторник в 19:00
//каждый день в 19:00
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

        this.timeLexer = new TimeLexer(timeLexerConfig, StringUtils.reverseDelimited(parts[0], ' '));
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
        LinkedList<BaseLexem> lexems = new LinkedList<>();

        String tokenizeStr = StringUtils.reverseDelimited(parts[0], ' ');
        GroupMatcher repeatTimeMatcher = lexerConfig.getRepeatTimePattern().matcher(tokenizeStr);

        if (repeatTimeMatcher.find()) {
            Map<String, String> repeatTimeValues = repeatTimeMatcher.values();

            lexems.add(new ReminderLexem(ReminderToken.REPEAT, ""));
            if (repeatTimeValues.containsKey(PatternBuilder.HOURS)) {
                lexems.add(new ReminderLexem(ReminderToken.HOURS, repeatTimeValues.get(PatternBuilder.HOURS)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.MINUTES)) {
                lexems.add(new ReminderLexem(ReminderToken.MINUTES, repeatTimeValues.get(PatternBuilder.MINUTES)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
                lexems.add(new ReminderLexem(ReminderToken.DAY_OF_WEEK, repeatTimeValues.get(PatternBuilder.DAY_OF_WEEK_WORD)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.ONE_DAY)) {
                lexems.add(new ReminderLexem(ReminderToken.ONE_DAY, repeatTimeValues.get(PatternBuilder.ONE_DAY)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.DAYS)) {
                lexems.add(new ReminderLexem(ReminderToken.DAYS, repeatTimeValues.get(PatternBuilder.DAYS)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new ReminderLexem(ReminderToken.HOUR, repeatTimeValues.get(PatternBuilder.HOUR)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.MINUTE)) {
                lexems.add(new ReminderLexem(ReminderToken.MINUTE, repeatTimeValues.get(PatternBuilder.MINUTE)));
            }
            tokenizeStr = StringUtils.reverseDelimited(tokenizeStr.substring(repeatTimeMatcher.end()).trim(), ' ');

            lexems.addFirst(new ReminderLexem(ReminderToken.TEXT, tokenizeStr.trim()));
            if (parts.length > 1) {
                lexems.add(new ReminderLexem(ReminderToken.NOTE, parts[1]));
            }

            return lexems;
        }

        return null;
    }

    private LinkedList<BaseLexem> tokenizeStandardRequest() {
        LinkedList<BaseLexem> lexems = new LinkedList<>(timeLexer.tokenize());

        String tokenizeStr = StringUtils.reverseDelimited(parts[0], ' ');
        tokenizeStr = StringUtils.reverseDelimited(tokenizeStr.substring(timeLexer.end()).trim(), ' ');

        return tokenizeReminderText(tokenizeStr, lexems);
    }

    private LinkedList<BaseLexem> tokenizeReminderText(String tokenizeStr, LinkedList<BaseLexem> lexems) {
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
