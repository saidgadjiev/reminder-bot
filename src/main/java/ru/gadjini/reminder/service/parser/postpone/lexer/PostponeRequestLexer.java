package ru.gadjini.reminder.service.parser.postpone.lexer;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostponeRequestLexer {

    private PostponeLexerConfig lexerConfig;

    private TimeLexer timeLexer;

    private String str;

    public PostponeRequestLexer(PostponeLexerConfig lexerConfig, TimeLexerConfig timeLexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str.toLowerCase();
        this.timeLexer = new TimeLexer(timeLexerConfig, StringUtils.reverseDelimited(str, ' '));
    }

    public List<BaseLexem> tokenize() {
        List<BaseLexem> lexems = new ArrayList<>();
        int firstSpaceIndexOf = str.indexOf(' ');
        String type = str.substring(0, firstSpaceIndexOf);

        lexems.add(new PostponeLexem(PostponeToken.TYPE, type));
        str = str.substring(firstSpaceIndexOf).trim();

        List<BaseLexem> postponeLexems = parseOnType();

        if (postponeLexems != null) {
            lexems.addAll(postponeLexems);

            return lexems;
        }
        postponeLexems = timeLexer.tokenize();

        if (postponeLexems != null) {
            lexems.addAll(postponeLexems);

            return lexems;
        }

        throw new ParseException();
    }

    private List<BaseLexem> parseOnType() {
        GroupPattern pattern = lexerConfig.getPattern(ParsedPostponeTime.Type.ON);
        GroupMatcher matcher = pattern.matcher(str);

        if (matcher.matches()) {
            Map<String, String> values = matcher.values();

            return toLexems(values);
        }

        return null;
    }

    private List<BaseLexem> toLexems(Map<String, String> values) {
        List<BaseLexem> lexems = new ArrayList<>();

        if (values.containsKey(PatternBuilder.DAYS)) {
            lexems.add(new PostponeLexem(PostponeToken.ON_DAY, values.get(PatternBuilder.DAYS)));
        }

        if (values.containsKey(PatternBuilder.HOURS)) {
            lexems.add(new PostponeLexem(PostponeToken.ON_HOUR, values.get(PatternBuilder.HOURS)));
        }

        if (values.containsKey(PatternBuilder.MINUTES)) {
            lexems.add(new PostponeLexem(PostponeToken.ON_MINUTE, values.get(PatternBuilder.MINUTES)));
        }

        return lexems;
    }
}
