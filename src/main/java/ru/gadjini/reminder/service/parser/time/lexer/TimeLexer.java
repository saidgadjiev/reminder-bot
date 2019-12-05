package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeLexer {

    private TimeLexerConfig lexerConfig;

    private String str;

    private int matchEnd;

    public TimeLexer(TimeLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str;
    }

    public List<BaseLexem> tokenize() {
        GroupPattern pattern = lexerConfig.getPattern();
        GroupMatcher timeMatcher = pattern.matcher(str);

        if (timeMatcher.find()) {
            Map<String, String> values = timeMatcher.values();

            matchEnd = timeMatcher.end();

            return toLexems(values);
        }

        return null;
    }

    public int end() {
        return matchEnd;
    }

    private List<BaseLexem> toLexems(Map<String, String> values) {
        List<BaseLexem> lexems = new ArrayList<>();

        if (values.containsKey(PatternBuilder.DAY_WORD)) {
            lexems.add(new TimeLexem(TimeToken.DAY_WORD, values.get(PatternBuilder.DAY_WORD)));
        }
        if (values.containsKey(PatternBuilder.MONTH)) {
            lexems.add(new TimeLexem(TimeToken.MONTH, values.get(PatternBuilder.MONTH)));
        }
        if (values.containsKey(PatternBuilder.DAY)) {
            lexems.add(new TimeLexem(TimeToken.DAY, values.get(PatternBuilder.DAY)));
        }
        if (values.containsKey(PatternBuilder.MONTH_WORD)) {
            lexems.add(new TimeLexem(TimeToken.MONTH_WORD, values.get(PatternBuilder.MONTH_WORD)));
        }
        if (values.containsKey(PatternBuilder.NEXT_WEEK)) {
            lexems.add(new TimeLexem(TimeToken.NEXT_WEEK, values.get(PatternBuilder.NEXT_WEEK)));
        }
        if (values.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
            lexems.add(new TimeLexem(TimeToken.DAY_OF_WEEK, values.get(PatternBuilder.DAY_OF_WEEK_WORD)));
        }

        lexems.add(new TimeLexem(TimeToken.HOUR, values.get(PatternBuilder.HOUR)));
        lexems.add(new TimeLexem(TimeToken.MINUTE, values.get(PatternBuilder.MINUTE)));

        return lexems;
    }

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))( )?(в )?((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)( (?<nextweek>следующ(ий|ей|ую|ее)|след))?( (во|в))?( )?)?((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )?(((?<month>1[0-2]|[1-9])\\.)?(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>завтра|послезавтра))?");

        Matcher matcher = pattern.matcher("17:30 Физиотерапевт");

        if (matcher.find()) {
            System.out.println(matcher.group("hour"));
            System.out.println(matcher.group("minute"));
            System.out.println(matcher.end());
        }
    }
}
