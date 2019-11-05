package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.pattern.GroupMatcher;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReminderRequestLexer {

    private final ReminderRequestLexerConfig lexerConfig;

    private LinkedList<ReminderLexem> lexems = new LinkedList<>();

    private int timeMatcherEnd;

    private final String str;

    public ReminderRequestLexer(ReminderRequestLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str.toLowerCase();
    }

    public List<ReminderLexem> tokenize() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ' );
        tokenizeTimePart();
        tokenizeStr = StringUtils.reverseDelimited(tokenizeStr.substring(timeMatcherEnd).trim(), ' ' );

        GroupPattern loginPattern = lexerConfig.getLoginPattern();
        GroupMatcher loginMatcher = loginPattern.matcher(tokenizeStr);

        if (loginMatcher.matches()) {
            Map<String, String> values = loginMatcher.values();

            lexems.addFirst(new ReminderLexem(ReminderToken.TEXT, values.get(PatternBuilder.TEXT)));
            lexems.addFirst(new ReminderLexem(ReminderToken.LOGIN, values.get(PatternBuilder.LOGIN)));

            return lexems;
        }

        throw new ParseException();
    }

    public List<ReminderLexem> tokenizeTime() {
        tokenizeTimePart();

        if (lexems.size() > 0) {
            return lexems;
        }

        throw new ParseException();
    }

    private void tokenizeTimePart() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ' );
        GroupPattern timePattern = lexerConfig.getTimePattern();
        GroupMatcher timeMatcher = timePattern.matcher(tokenizeStr);

        if (timeMatcher.find()) {
            Map<String, String> values = timeMatcher.values();

            lexems.add(new ReminderLexem(ReminderToken.MINUTE, values.get(PatternBuilder.MINUTE)));
            lexems.add(new ReminderLexem(ReminderToken.HOUR, values.get(PatternBuilder.HOUR)));

            if (values.containsKey(PatternBuilder.MONTH_WORD)) {
                lexems.add(new ReminderLexem(ReminderToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
            }

            if (values.containsKey(PatternBuilder.DAY)) {
                lexems.add(new ReminderLexem(ReminderToken.DAY, values.get(PatternBuilder.DAY)));
            }

            if (values.containsKey(PatternBuilder.MONTH)) {
                lexems.add(new ReminderLexem(ReminderToken.MONTH, values.get(PatternBuilder.MONTH)));
            }
            if (values.containsKey(PatternBuilder.DAY_WORD)) {
                lexems.add(new ReminderLexem(ReminderToken.DAY_WORD, values.get(PatternBuilder.DAY_WORD)));
            }

            timeMatcherEnd = timeMatcher.end();
        }
    }
}
