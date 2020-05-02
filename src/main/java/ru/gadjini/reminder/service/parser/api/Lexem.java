package ru.gadjini.reminder.service.parser.api;

public class Lexem {

    private Token token;

    private String value;

    public Lexem(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lexem timeLexem = (Lexem) o;

        if (token != timeLexem.token) return false;
        return getValue() != null ? getValue().equals(timeLexem.getValue()) : timeLexem.getValue() == null;
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
                "timeToken=" + token + " " +
                "value=" + getValue() +
                '}';
    }
}
