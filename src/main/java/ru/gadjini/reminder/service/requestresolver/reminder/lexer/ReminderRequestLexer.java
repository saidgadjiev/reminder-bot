package ru.gadjini.reminder.service.requestresolver.reminder.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ParseException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderRequestLexer {

    private Pattern timePattern;

    private Pattern loginPattern;

    private int timeMatcherEnd;

    private final String str;

    public ReminderRequestLexer(Pattern timePattern, Pattern loginPattern, String str) {
        this.timePattern = timePattern;
        this.loginPattern = loginPattern;
        this.str = str.toLowerCase();
    }

    public List<ReminderLexem> tokenize() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ');
        LinkedList<ReminderLexem> lexems = (LinkedList<ReminderLexem>) tokenizeTime();

        tokenizeStr = StringUtils.reverseDelimited(tokenizeStr.substring(timeMatcherEnd).trim(), ' ');

        Matcher loginMatcher = loginPattern.matcher(tokenizeStr);

        if (loginMatcher.matches()) {
            for (ReminderToken token : Arrays.asList(ReminderToken.TEXT, ReminderToken.LOGIN)) {
                String found = loginMatcher.group(token.name().toLowerCase());

                if (found != null) {
                    lexems.addFirst(new ReminderLexem(token, found));
                }
            }

            return lexems;
        }

        throw new ParseException();
    }

    public List<ReminderLexem> tokenizeTime() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ');

        Matcher timeMatcher = timePattern.matcher(tokenizeStr);

        if (timeMatcher.find()) {
            LinkedList<ReminderLexem> lexems = new LinkedList<>();

            for (ReminderToken token : Arrays.asList(ReminderToken.MINUTE, ReminderToken.HOUR, ReminderToken.MONTHWORD, ReminderToken.DAY, ReminderToken.MONTH, ReminderToken.DAYWORD)) {
                String found = timeMatcher.group(token.name().toLowerCase());

                if (found != null) {
                    lexems.addFirst(new ReminderLexem(token, found));
                }
            }

            timeMatcherEnd = timeMatcher.end();

            return lexems;
        }

        throw new ParseException();
    }
}
