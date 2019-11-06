package ru.gadjini.reminder.service.parser.remind.lexer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;

@Component
public class CustomRemindLexerConfig {

    private GroupPattern pattern;

    @Autowired
    public CustomRemindLexerConfig(PatternBuilder patternBuilder) {
        pattern = patternBuilder.buildCustomRemindPattern();
    }

    public GroupPattern getPattern() {
        return pattern;
    }
}
