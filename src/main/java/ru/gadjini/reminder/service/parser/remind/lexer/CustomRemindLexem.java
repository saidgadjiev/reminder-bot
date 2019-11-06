package ru.gadjini.reminder.service.parser.remind.lexer;

public class CustomRemindLexem {

    private CustomReminderToken token;

    private String value;

    public CustomRemindLexem(CustomReminderToken token, String value) {
        this.token = token;
        this.value = value;
    }

    public CustomReminderToken getToken() {
        return token;
    }

    public void setToken(CustomReminderToken token) {
        this.token = token;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
