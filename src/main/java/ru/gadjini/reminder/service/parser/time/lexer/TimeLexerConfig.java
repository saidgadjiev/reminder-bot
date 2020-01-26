package ru.gadjini.reminder.service.parser.time.lexer;

import org.springframework.stereotype.Component;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class TimeLexerConfig {

    private Map<Locale, GroupPattern> timePatterns = new HashMap<>();

    private final Map<Locale, GroupPattern> repeatTimePatterns = new HashMap<>();

    private final Map<Locale, GroupPattern> offsetTimePatterns = new HashMap<>();

    private final Map<Locale, GroupPattern> repeatWordPatterns = new HashMap<>();

    public TimeLexerConfig(PatternBuilder patternBuilder) {
        timePatterns.put(Locale.getDefault(), patternBuilder.buildTimePattern(Locale.getDefault()));
        repeatTimePatterns.put(Locale.getDefault(), patternBuilder.buildRepeatTimePattern(Locale.getDefault()));
        offsetTimePatterns.put(Locale.getDefault(), patternBuilder.buildOffsetTimePattern());
        repeatWordPatterns.put(Locale.getDefault(), patternBuilder.buildRepeatWordPattern());
    }

    public GroupPattern getTimePattern() {
        return timePatterns.get(Locale.getDefault());
    }

    public GroupPattern getRepeatTimePattern() {
        return repeatTimePatterns.get(Locale.getDefault());
    }

    public GroupPattern getOffsetTimePattern() {
        return offsetTimePatterns.get(Locale.getDefault());
    }

    public GroupPattern getRepeatWordPattern() {
        return repeatWordPatterns.get(Locale.getDefault());
    }
}
