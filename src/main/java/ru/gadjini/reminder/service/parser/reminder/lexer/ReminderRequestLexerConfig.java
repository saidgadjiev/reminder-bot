package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReminderRequestLexerConfig {

    private static final String DEFAULT_NOTE_START = ";";

    private Map<String, String> textAndNoteBreakPatterns = new HashMap<>();

    @Autowired
    public ReminderRequestLexerConfig(LocalisationService localisationService) {
        for (Locale locale: localisationService.getSupportedLocales()) {
            textAndNoteBreakPatterns.put(locale.getLanguage(), localisationService.getMessage(MessagesProperties.REGEXP_NOTE_START, locale) + "|" + DEFAULT_NOTE_START);
        }
    }

    public String getTextAndNoteBreakPattern(Locale locale) {
        return textAndNoteBreakPatterns.get(locale.getLanguage());
    }
}
