package ru.gadjini.reminder.service.requestresolver.postpone.lexer;

public class PostponeLexem {

    private PostponeToken token;

    private String value;

    PostponeLexem(PostponeToken token, String value) {
        this.token = token;
        this.value = value;
    }

    public PostponeToken getToken() {
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
