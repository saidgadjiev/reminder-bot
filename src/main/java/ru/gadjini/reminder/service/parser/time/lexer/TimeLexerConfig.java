package ru.gadjini.reminder.service.parser.time.lexer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.message.LocalisationService;
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

    @Autowired
    public TimeLexerConfig(LocalisationService localisationService, PatternBuilder patternBuilder) {
        for (Locale locale : localisationService.getSupportedLocales()) {
            timePatterns.put(locale, patternBuilder.buildTimePattern(locale));
            repeatTimePatterns.put(locale, patternBuilder.buildRepeatTimePattern(locale));
            offsetTimePatterns.put(locale, patternBuilder.buildOffsetTimePattern(locale));
            repeatWordPatterns.put(locale, patternBuilder.buildRepeatWordPattern(locale));
        }
    }

    public GroupPattern getTimePattern(Locale locale) {
        return timePatterns.get(locale);
    }

    public GroupPattern getRepeatTimePattern(Locale locale) {
        return repeatTimePatterns.get(locale);
    }

    public GroupPattern getOffsetTimePattern(Locale locale) {
        return offsetTimePatterns.get(locale);
    }

    public GroupPattern getRepeatWordPattern(Locale locale) {
        return repeatWordPatterns.get(locale);
    }
}
