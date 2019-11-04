package ru.gadjini.reminder.service.requestresolver.postpone.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.service.requestresolver.ParseException;
import ru.gadjini.reminder.service.requestresolver.postpone.parser.ParsedPostponeTime;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostponeRequestLexer {

    private final Map<ParsedPostponeTime.Type, Pattern> patterns;

    private String str;

    public PostponeRequestLexer(Map<ParsedPostponeTime.Type, Pattern> patterns, String str) {
        this.patterns = patterns;
        this.str = str.toLowerCase();
    }

    public List<PostponeLexem> tokenize() {
        List<PostponeLexem> lexems = new ArrayList<>();
        int firstSpaceIndexOf = str.indexOf(' ' );
        String type = str.substring(0, firstSpaceIndexOf);

        lexems.add(new PostponeLexem(PostponeToken.TYPE, type));
        str = str.substring(firstSpaceIndexOf).trim();

        List<PostponeLexem> postponeLexems = parseOnType(patterns.get(ParsedPostponeTime.Type.ON));

        if (postponeLexems != null) {
            lexems.addAll(postponeLexems);

            return lexems;
        }
        postponeLexems = parseAtType(patterns.get(ParsedPostponeTime.Type.AT));

        if (postponeLexems != null) {
            lexems.addAll(postponeLexems);

            return lexems;
        }

        throw new ParseException();
    }

    private List<PostponeLexem> parseOnType(Pattern pattern) {
        Matcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            List<PostponeLexem> lexems = new ArrayList<>();

            for (PostponeToken token : Arrays.asList(PostponeToken.ONDAY, PostponeToken.ONHOUR, PostponeToken.ONMINUTE)) {
                String value = matcher.group(token.name().toLowerCase());

                if (value != null) {
                    lexems.add(new PostponeLexem(token, value));
                }
            }

            return lexems;
        }

        return null;
    }

    private List<PostponeLexem> parseAtType(Pattern pattern) {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ' );

        Matcher timeMatcher = pattern.matcher(tokenizeStr);

        if (timeMatcher.matches()) {
            LinkedList<PostponeLexem> lexems = new LinkedList<>();

            for (PostponeToken token : Arrays.asList(PostponeToken.MINUTE, PostponeToken.HOUR, PostponeToken.MONTHWORD, PostponeToken.DAY, PostponeToken.MONTH, PostponeToken.DAYWORD)) {
                String found = timeMatcher.group(token.name().toLowerCase());

                if (found != null) {
                    lexems.addFirst(new PostponeLexem(token, found));
                }
            }

            return lexems;
        }

        return null;
    }
}
