package ru.gadjini.reminder.service.parser.remind.lexer;

import ru.gadjini.reminder.service.parser.api.BaseLexem;

public class CustomRemindLexem extends BaseLexem {

    private CustomRemindToken token;

    public CustomRemindLexem(CustomRemindToken token, String value) {
        super(value);
        this.token = token;
    }

    public CustomRemindToken getToken() {
        return token;
    }

    public void setToken(CustomRemindToken token) {
        this.token = token;
    }
}
