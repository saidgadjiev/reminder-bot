package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReminderRequestLexerConfig {

    private static final String DEFAULT_PART_START = ";";

    private Map<String, String> requestPartsBreakPatterns = new HashMap<>();

    @Autowired
    public ReminderRequestLexerConfig(LocalisationService localisationService) {
        for (Locale locale: localisationService.getSupportedLocales()) {
            requestPartsBreakPatterns.put(locale.getLanguage(), DEFAULT_PART_START);
        }
    }

    public String getRequestPartsBreakPattern(Locale locale) {
        return requestPartsBreakPatterns.get(locale.getLanguage());
    }
}
