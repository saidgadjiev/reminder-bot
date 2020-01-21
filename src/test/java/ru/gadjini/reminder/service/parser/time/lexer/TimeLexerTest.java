package ru.gadjini.reminder.service.parser.time.lexer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.pattern.Patterns;

class TimeLexerTest {

    private static final TimeLexerConfig TIME_LEXER_CONFIG = Mockito.mock(TimeLexerConfig.class);

    static {
        Mockito.when(TIME_LEXER_CONFIG.getTimePattern()).thenReturn(new GroupPattern(Patterns.FIXED_TIME_PATTERN, PatternBuilder.FIXED_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern()).thenReturn(new GroupPattern(Patterns.OFFSET_TIME_PATTERN, PatternBuilder.OFFSET_TIME_PATTERN_GROUPS));
        Mockito.when(TIME_LEXER_CONFIG.getOffsetTimePattern()).thenReturn(new GroupPattern(Patterns.REPEAT_TIME_PATTERN, PatternBuilder.REPEAT_TIME_PATTERN_GROUPS));
    }

    @Test
    void tokenizeThrowParseException() {
    }

    @Test
    void tokenize() {
    }

    @Test
    void eraseTime() {
    }
}