package ru.gadjini.reminder.service.parser.time.lexer;

import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.Token;

public class TimeLexem extends BaseLexem {

    private TimeToken timeToken;

    public TimeLexem(TimeToken timeToken, String value) {
        super(value);
        this.timeToken = timeToken;
    }

    @Override
    public Token getToken() {
        return timeToken;
    }
}
