package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.pattern.GroupMatcher;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ReminderRequestLexer {

    private final ReminderRequestLexerConfig lexerConfig;

    private LinkedList<ReminderLexem> lexems = new LinkedList<>();

    private int timeMatcherEnd;

    private String str;

    public ReminderRequestLexer(ReminderRequestLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str;
    }

    public List<ReminderLexem> tokenize() {
        String[] parts = str.split(";");

        str = parts[0].trim();
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ' );
        tokenizeTimePart();
        tokenizeStr = StringUtils.reverseDelimited(tokenizeStr.substring(timeMatcherEnd).trim(), ' ' );

        GroupPattern loginPattern = lexerConfig.getLoginPattern();
        GroupMatcher loginMatcher = loginPattern.matcher(tokenizeStr);

        if (loginMatcher.matches()) {
            Map<String, String> values = loginMatcher.values();

            lexems.addFirst(new ReminderLexem(ReminderToken.TEXT, values.get(PatternBuilder.TEXT).trim()));
            if (values.containsKey(PatternBuilder.LOGIN)) {
                lexems.addFirst(new ReminderLexem(ReminderToken.LOGIN, values.get(PatternBuilder.LOGIN).trim()));
            }
            if (parts.length > 1) {
                lexems.add(new ReminderLexem(ReminderToken.NOTE, parts[1].trim()));
            }

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

            if (values.containsKey(PatternBuilder.DAY_WORD)) {
                lexems.add(new ReminderLexem(ReminderToken.DAY_WORD, values.get(PatternBuilder.DAY_WORD)));
            }
            if (values.containsKey(PatternBuilder.MONTH)) {
                lexems.add(new ReminderLexem(ReminderToken.MONTH, values.get(PatternBuilder.MONTH)));
            }
            if (values.containsKey(PatternBuilder.DAY)) {
                lexems.add(new ReminderLexem(ReminderToken.DAY, values.get(PatternBuilder.DAY)));
            }
            if (values.containsKey(PatternBuilder.MONTH_WORD)) {
                lexems.add(new ReminderLexem(ReminderToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
            }

            lexems.add(new ReminderLexem(ReminderToken.HOUR, values.get(PatternBuilder.HOUR)));
            lexems.add(new ReminderLexem(ReminderToken.MINUTE, values.get(PatternBuilder.MINUTE)));

            timeMatcherEnd = timeMatcher.end();
        }
    }

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("(@(?<login>[0-9a-zA-Z_]+) )?(?<text>[a-zA-Zа-яА-ЯёЁ1-9 ]+)$");

        System.out.println(pattern.matcher("Самолёт отца").matches());
    }
}
