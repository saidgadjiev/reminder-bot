package ru.gadjini.reminder.service.resolver.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.service.resolver.parser.ParseException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestLexer {

    private Pattern timeStartPattern;

    private Pattern loginStartPattern;

    private int timeMatcherEnd;

    private final String str;

    public RequestLexer(Pattern timeStartPattern, Pattern loginStartPattern, String str) {
        this.timeStartPattern = timeStartPattern;
        this.loginStartPattern = loginStartPattern;
        this.str = str;
    }

    public List<Lexem> tokenize() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ');
        LinkedList<Lexem> lexems = (LinkedList<Lexem>) tokenizeTime();

        tokenizeStr = StringUtils.reverseDelimited(tokenizeStr.substring(timeMatcherEnd).trim(), ' ');

        Matcher loginStartMatcher = loginStartPattern.matcher(tokenizeStr);

        if (loginStartMatcher.matches()) {
            for (Token token : Arrays.asList(Token.TEXT, Token.LOGIN)) {
                String found = loginStartMatcher.group(token.name().toLowerCase());

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

        Matcher timeStartMatcher = timeStartPattern.matcher(tokenizeStr);

        if (timeStartMatcher.find()) {
            LinkedList<Lexem> lexems = new LinkedList<>();

            for (Token token : Arrays.asList(Token.MINUTE, Token.HOUR, Token.DAY, Token.DAYWORD)) {
                String found = timeStartMatcher.group(token.name().toLowerCase());

                if (found != null) {
                    lexems.addFirst(new Lexem(token, found));
                }
            }

            timeMatcherEnd = timeStartMatcher.end();

            return lexems;
        }

        throw new ParseException();
    }
}
