package ru.gadjini.reminder.service.resolver.lexer;

public class Lexem {

    private Token token;

    private String value;

    Lexem(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    public Token getToken() {
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
