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

    private Map<String, GroupPattern> timePatterns = new HashMap<>();

    private final Map<String, GroupPattern> repeatTimePatterns = new HashMap<>();

    private final Map<String, GroupPattern> offsetTimePatterns = new HashMap<>();

    private final Map<String, GroupPattern> repeatWordPatterns = new HashMap<>();

    @Autowired
    public TimeLexerConfig(LocalisationService localisationService, PatternBuilder patternBuilder) {
        for (Locale locale : localisationService.getSupportedLocales()) {
            timePatterns.put(locale.getLanguage(), patternBuilder.buildTimePattern(locale));
            repeatTimePatterns.put(locale.getLanguage(), patternBuilder.buildRepeatTimePattern(locale));
            offsetTimePatterns.put(locale.getLanguage(), patternBuilder.buildOffsetTimePattern(locale));
            repeatWordPatterns.put(locale.getLanguage(), patternBuilder.buildRepeatWordPattern(locale));
        }
    }

    public GroupPattern getTimePattern(Locale locale) {
        return timePatterns.get(locale.getLanguage());
    }

    public GroupPattern getRepeatTimePattern(Locale locale) {
        return repeatTimePatterns.get(locale.getLanguage());
    }

    public GroupPattern getOffsetTimePattern(Locale locale) {
        return offsetTimePatterns.get(locale.getLanguage());
    }

    public GroupPattern getRepeatWordPattern(Locale locale) {
        return repeatWordPatterns.get(locale.getLanguage());
    }
}
