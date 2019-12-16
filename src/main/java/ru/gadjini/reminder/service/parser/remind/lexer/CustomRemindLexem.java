package ru.gadjini.reminder.service.parser.remind.lexer;

import ru.gadjini.reminder.service.parser.api.BaseLexem;

public class CustomRemindLexem extends BaseLexem {

    private CustomRemindToken token;

    private String value;

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

    public void setValue(String value) {
        this.value = value;
    }
}
