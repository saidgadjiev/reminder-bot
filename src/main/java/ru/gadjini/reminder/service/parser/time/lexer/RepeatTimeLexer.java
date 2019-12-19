package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.regex.GroupMatcher;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RepeatTimeLexer {

    private TimeLexerConfig lexerConfig;

    private String str;

    private int matchEnd;

    public RepeatTimeLexer(TimeLexerConfig lexerConfig, String str) {
        this.lexerConfig = lexerConfig;
        this.str = str;
    }

    public List<BaseLexem> tokenize() {
        LinkedList<BaseLexem> lexems = new LinkedList<>();

        GroupMatcher repeatTimeMatcher = lexerConfig.getRepeatTimePattern().maxMatcher(str);

        if (repeatTimeMatcher.matches()) {
            Map<String, String> repeatTimeValues = repeatTimeMatcher.values();

            if (repeatTimeValues.containsKey(PatternBuilder.HOURS)) {
                lexems.add(new TimeLexem(TimeToken.HOURS, repeatTimeValues.get(PatternBuilder.HOURS)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.EVERY_HOUR)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_HOUR, repeatTimeValues.get(PatternBuilder.EVERY_HOUR)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.MINUTES)) {
                lexems.add(new TimeLexem(TimeToken.MINUTES, repeatTimeValues.get(PatternBuilder.MINUTES)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.DAY_OF_WEEK_WORD)) {
                lexems.add(new TimeLexem(TimeToken.DAY_OF_WEEK, repeatTimeValues.get(PatternBuilder.DAY_OF_WEEK_WORD)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.EVERY_DAY)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_DAY, repeatTimeValues.get(PatternBuilder.EVERY_DAY)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.EVERY_MINUTE)) {
                lexems.add(new TimeLexem(TimeToken.EVERY_MINUTE, repeatTimeValues.get(PatternBuilder.EVERY_MINUTE)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.DAYS)) {
                lexems.add(new TimeLexem(TimeToken.DAYS, repeatTimeValues.get(PatternBuilder.DAYS)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.HOUR)) {
                lexems.add(new TimeLexem(TimeToken.HOUR, repeatTimeValues.get(PatternBuilder.HOUR)));
            }
            if (repeatTimeValues.containsKey(PatternBuilder.MINUTE)) {
                lexems.add(new TimeLexem(TimeToken.MINUTE, repeatTimeValues.get(PatternBuilder.MINUTE)));
            }
            matchEnd = repeatTimeMatcher.end();

            return lexems;
        }

        return null;
    }

    public int end() {
        return matchEnd;
    }
}
