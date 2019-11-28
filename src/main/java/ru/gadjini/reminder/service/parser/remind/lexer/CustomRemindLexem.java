package ru.gadjini.reminder.service.parser.remind.lexer;

public class CustomRemindLexem {

    private CustomRemindToken token;

    private String value;

    public CustomRemindLexem(CustomRemindToken token, String value) {
        this.token = token;
        this.value = value;
    }

    public CustomRemindToken getToken() {
        return token;
    }

    public void setToken(CustomRemindToken token) {
        this.token = token;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
