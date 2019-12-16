package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReminderRequestLexerConfig {

    private final GroupPattern loginPattern;

    public ReminderRequestLexerConfig(PatternBuilder patternBuilder) {
        loginPattern = patternBuilder.buildLoginPattern();
    }

    public GroupPattern getLoginPattern() {
        return loginPattern;
    }
}
