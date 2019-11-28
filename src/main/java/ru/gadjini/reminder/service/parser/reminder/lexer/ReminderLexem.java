package ru.gadjini.reminder.service.parser.reminder.lexer;

import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.Token;

public class ReminderLexem extends BaseLexem {

    private final ReminderToken token;

    ReminderLexem(ReminderToken token, String value) {
        super(value);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "Lexem{" +
                "token=" + token +
                ", value='" + getValue() + '\'' +
                '}';
    }
}
