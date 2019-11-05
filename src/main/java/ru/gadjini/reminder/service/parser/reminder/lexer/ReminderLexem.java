package ru.gadjini.reminder.service.parser.reminder.lexer;

public class ReminderLexem {

    private ReminderToken token;

    private String value;

    ReminderLexem(ReminderToken token, String value) {
        this.token = token;
        this.value = value;
    }

    public ReminderToken getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Lexem{" +
                "token=" + token +
                ", value='" + value + '\'' +
                '}';
    }
}
