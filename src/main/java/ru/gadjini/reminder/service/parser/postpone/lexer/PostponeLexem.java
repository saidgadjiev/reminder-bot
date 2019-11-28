package ru.gadjini.reminder.service.parser.postpone.lexer;

import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.Token;

public class PostponeLexem extends BaseLexem {

    private PostponeToken token;

    PostponeLexem(PostponeToken token, String value) {
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
