package ru.gadjini.reminder.service.resolver.lexer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.parameters.P;
import ru.gadjini.reminder.service.resolver.parser.ParseException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestLexer {

    private Pattern timePattern;

    private Pattern loginPattern;

    private int timeMatcherEnd;

    private final String str;

    public RequestLexer(Pattern timePattern, Pattern loginPattern, String str) {
        this.timePattern = timePattern;
        this.loginPattern = loginPattern;
        this.str = str;
    }

    public List<Lexem> tokenize() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ');
        LinkedList<Lexem> lexems = (LinkedList<Lexem>) tokenizeTime();

        tokenizeStr = StringUtils.reverseDelimited(tokenizeStr.substring(timeMatcherEnd).trim(), ' ');

        Matcher loginMatcher = loginPattern.matcher(tokenizeStr);

        if (loginMatcher.matches()) {
            for (Token token : Arrays.asList(Token.TEXT, Token.LOGIN)) {
                String found = loginMatcher.group(token.name().toLowerCase());

                if (found != null) {
                    lexems.addFirst(new Lexem(token, found));
                }
            }

            return lexems;
        }

        throw new ParseException();
    }

    public List<Lexem> tokenizeTime() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ');

        Matcher timeMatcher = timePattern.matcher(tokenizeStr);

        if (timeMatcher.find()) {
            LinkedList<Lexem> lexems = new LinkedList<>();

            for (Token token : Arrays.asList(Token.MINUTE, Token.HOUR, Token.DAY, Token.DAYWORD)) {
                String found = timeMatcher.group(token.name().toLowerCase());

                if (found != null) {
                    lexems.addFirst(new Lexem(token, found));
                }
            }

            timeMatcherEnd = timeMatcher.end();

            return lexems;
        }

        throw new ParseException();
    }
}
