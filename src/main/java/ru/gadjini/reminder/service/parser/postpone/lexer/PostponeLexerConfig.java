package ru.gadjini.reminder.service.parser.postpone.lexer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PostponeLexerConfig {

    private Map<ParsedPostponeTime.Type, GroupPattern> patterns = new HashMap<>();

    public PostponeLexerConfig(PatternBuilder patternBuilder) {
        patterns.put(ParsedPostponeTime.Type.AT, patternBuilder.buildTimePattern(Locale.getDefault()));
        patterns.put(ParsedPostponeTime.Type.ON, patternBuilder.buildPostponePattern());
    }

    public GroupPattern getPattern(ParsedPostponeTime.Type type) {
        return patterns.get(type);
    }
}
