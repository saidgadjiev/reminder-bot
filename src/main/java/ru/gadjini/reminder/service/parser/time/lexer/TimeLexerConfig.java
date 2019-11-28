package ru.gadjini.reminder.service.parser.time.lexer;

import org.springframework.stereotype.Component;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class TimeLexerConfig {

    private Map<Locale, GroupPattern> patterns = new HashMap<>();

    public TimeLexerConfig(PatternBuilder patternBuilder) {
        patterns.put(Locale.getDefault(), patternBuilder.buildTimePattern(Locale.getDefault()));
    }

    public GroupPattern getPattern() {
        return patterns.get(Locale.getDefault());
    }
}
