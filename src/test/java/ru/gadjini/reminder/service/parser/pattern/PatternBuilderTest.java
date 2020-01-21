package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.Locale;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { LocalisationService.class, DayOfWeekService.class, PatternBuilder.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class PatternBuilderTest {

    @Autowired
    private PatternBuilder patternBuilder;

    @Test
    void buildRepeatTimePattern() {
        GroupPattern timePattern = patternBuilder.buildRepeatTimePattern(Locale.getDefault());

        Assert.assertEquals(Patterns.REPEAT_TIME_PATTERN.pattern(), timePattern.getPattern());
    }

    @Test
    void buildTimePattern() {
        GroupPattern timePattern = patternBuilder.buildTimePattern(Locale.getDefault());

        Assert.assertEquals(Patterns.FIXED_TIME_PATTERN.pattern(), timePattern.getPattern());
    }

    @Test
    void buildOffsetTimePattern() {
        GroupPattern timePattern = patternBuilder.buildOffsetTimePattern();

        Assert.assertEquals(Patterns.OFFSET_TIME_PATTERN.pattern(), timePattern.getPattern());
    }
}