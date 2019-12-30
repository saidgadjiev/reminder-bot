package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReminderRequestLexerConfig {

    private static final String DEFAULT_NOTE_START = ";";

    private final GroupPattern loginPattern;

    private String textAndNoteBreakPattern;

    public ReminderRequestLexerConfig(LocalisationService localisationService, PatternBuilder patternBuilder) {
        loginPattern = patternBuilder.buildLoginPattern();

        textAndNoteBreakPattern = localisationService.getMessage(MessagesProperties.REGEXP_NOTE_START) + "|" + DEFAULT_NOTE_START;
    }

    public GroupPattern getLoginPattern() {
        return loginPattern;
    }

    public String getTextAndNoteBreakPattern() {
        return textAndNoteBreakPattern;
    }
}
