package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.message.LocalisationService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReminderRequestLexerConfig {

    private static final String DEFAULT_NOTE_START = ";";

    private String textAndNoteBreakPattern;

    public ReminderRequestLexerConfig(LocalisationService localisationService) {
        textAndNoteBreakPattern = localisationService.getMessage(MessagesProperties.REGEXP_NOTE_START) + "|" + DEFAULT_NOTE_START;
    }

    public String getTextAndNoteBreakPattern() {
        return textAndNoteBreakPattern;
    }
}
