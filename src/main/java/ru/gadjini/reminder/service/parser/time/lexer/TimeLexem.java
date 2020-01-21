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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeLexem timeLexem = (TimeLexem) o;

        if (timeToken != timeLexem.timeToken) return false;
        return getValue() != null ? getValue().equals(timeLexem.getValue()) : timeLexem.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = timeToken != null ? timeToken.hashCode() : 0;
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimeLexem{" +
                "timeToken=" + timeToken + " " +
                "value=" + getValue() +
                '}';
    }
}
