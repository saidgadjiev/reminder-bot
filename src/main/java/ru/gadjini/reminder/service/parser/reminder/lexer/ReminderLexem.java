package ru.gadjini.reminder.service.parser.reminder.lexer;

import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.api.Token;

public class ReminderLexem extends Lexem {

    private final ReminderToken token;

    public ReminderLexem(ReminderToken token, String value) {
        super(token, value);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReminderLexem that = (ReminderLexem) o;

        if (token != that.token) return false;
        return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Lexem{" +
                "token=" + token +
                ", value='" + getValue() + '\'' +
                '}';
    }
}
