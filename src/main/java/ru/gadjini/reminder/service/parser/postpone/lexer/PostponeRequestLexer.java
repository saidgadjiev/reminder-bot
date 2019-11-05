package ru.gadjini.reminder.service.parser.postpone.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.pattern.GroupMatcher;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.service.parser.ParseException;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostponeRequestLexer {

    private PostponeLexerConfig lexerConfig;

    private String str;

    public PostponeRequestLexer(PostponeLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str.toLowerCase();
    }

    public List<PostponeLexem> tokenize() {
        List<PostponeLexem> lexems = new ArrayList<>();
        int firstSpaceIndexOf = str.indexOf(' ' );
        String type = str.substring(0, firstSpaceIndexOf);

        lexems.add(new PostponeLexem(PostponeToken.TYPE, type));
        str = str.substring(firstSpaceIndexOf).trim();

        List<PostponeLexem> postponeLexems = parseOnType();

        if (postponeLexems != null) {
            lexems.addAll(postponeLexems);

            return lexems;
        }
        postponeLexems = parseAtType();

        if (postponeLexems != null) {
            lexems.addAll(postponeLexems);

            return lexems;
        }

        throw new ParseException();
    }

    private List<PostponeLexem> parseOnType() {
        GroupPattern pattern = lexerConfig.getPattern(ParsedPostponeTime.Type.ON);
        GroupMatcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            Map<String, String> values = matcher.values();

            return toLexems(values, ParsedPostponeTime.Type.ON);
        }

        return null;
    }

    private List<PostponeLexem> parseAtType() {
        String tokenizeStr = StringUtils.reverseDelimited(str, ' ' );

        GroupPattern pattern = lexerConfig.getPattern(ParsedPostponeTime.Type.AT);
        GroupMatcher timeMatcher = pattern.matcher(tokenizeStr);

        if (timeMatcher.matches()) {
            Map<String, String> values = timeMatcher.values();

            return toLexems(values, ParsedPostponeTime.Type.AT);
        }

        return null;
    }

    private List<PostponeLexem> toLexems(Map<String, String> values, ParsedPostponeTime.Type type) {
        switch (type) {
            case ON: {
                List<PostponeLexem> lexems = new ArrayList<>();

                if (values.containsKey(PatternBuilder.POSTPONE_DAY)) {
                    lexems.add(new PostponeLexem(PostponeToken.ON_DAY, values.get(PatternBuilder.POSTPONE_DAY)));
                }

                if (values.containsKey(PatternBuilder.POSTPONE_HOUR)) {
                    lexems.add(new PostponeLexem(PostponeToken.ON_HOUR, values.get(PatternBuilder.POSTPONE_HOUR)));
                }

                if (values.containsKey(PatternBuilder.POSTPONE_MINUTE)) {
                    lexems.add(new PostponeLexem(PostponeToken.ON_MINUTE, values.get(PatternBuilder.POSTPONE_MINUTE)));
                }

                return lexems;
            }
            case AT:
                List<PostponeLexem> lexems = new ArrayList<>();

                if (values.containsKey(PatternBuilder.DAY_WORD)) {
                    lexems.add(new PostponeLexem(PostponeToken.AT_DAY_WORD, values.get(PatternBuilder.DAY_WORD)));
                }
                if (values.containsKey(PatternBuilder.MONTH)) {
                    lexems.add(new PostponeLexem(PostponeToken.AT_MONTH, values.get(PatternBuilder.MONTH)));
                }
                if (values.containsKey(PatternBuilder.DAY)) {
                    lexems.add(new PostponeLexem(PostponeToken.AT_DAY, values.get(PatternBuilder.DAY)));
                }
                if (values.containsKey(PatternBuilder.MONTH_WORD)) {
                    lexems.add(new PostponeLexem(PostponeToken.AT_MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
                }

                lexems.add(new PostponeLexem(PostponeToken.AT_HOUR, values.get(PatternBuilder.HOUR)));
                lexems.add(new PostponeLexem(PostponeToken.AT_MINUTE, values.get(PatternBuilder.MINUTE)));

                return lexems;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
