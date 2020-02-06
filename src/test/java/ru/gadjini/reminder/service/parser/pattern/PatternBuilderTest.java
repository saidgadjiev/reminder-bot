package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.context.UserContextResolver;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.util.Locale;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserContextResolver.class, LocalisationService.class, DayOfWeekService.class, PatternBuilder.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class PatternBuilderTest {

    @Autowired
    private PatternBuilder patternBuilder;

    @SpyBean
    private LocalisationService localisationService;

    @BeforeEach
    void setUp() {
        Mockito.doReturn(new Locale("ru")).when(localisationService).getCurrentLocale("ru");
    }

    @Test
    void buildRepeatTimePattern() {
        GroupPattern timePattern = patternBuilder.buildRepeatTimePattern(localisationService.getCurrentLocale("ru"));

        Assert.assertEquals(Patterns.REPEAT_TIME_PATTERN.pattern(), timePattern.getPattern());
    }

    @Test
    void buildTimePattern() {
        GroupPattern timePattern = patternBuilder.buildTimePattern(localisationService.getCurrentLocale("ru"));

        Assert.assertEquals(Patterns.FIXED_TIME_PATTERN.pattern(), timePattern.getPattern());
    }

    @Test
    void buildOffsetTimePattern() {
        GroupPattern timePattern = patternBuilder.buildOffsetTimePattern(localisationService.getCurrentLocale("ru"));

        Assert.assertEquals(Patterns.OFFSET_TIME_PATTERN.pattern(), timePattern.getPattern());
    }
}