package ru.gadjini.reminder.service.parser.reminder.lexer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReminderRequestLexerConfig {

    private final GroupPattern loginPattern;

    private final Map<Locale, GroupPattern> timePatterns = new HashMap<>();

    public ReminderRequestLexerConfig(PatternBuilder patternBuilder) {
        loginPattern = patternBuilder.buildLoginPattern();
        timePatterns.put(Locale.getDefault(), patternBuilder.buildTimePattern(Locale.getDefault()));
    }

    public GroupPattern getLoginPattern() {
        return loginPattern;
    }

    public GroupPattern getTimePattern() {
        return timePatterns.get(Locale.getDefault());
    }
}
