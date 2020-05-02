package ru.gadjini.reminder.service.parser.api;

import ru.gadjini.reminder.exception.ParseException;

import java.util.List;

public class LexemsConsumer {

    private int position;

    public int getPosition() {
        return position;
    }

    public Lexem consume(List<Lexem> lexems, Token token) {
        Lexem lexem = get(lexems);

        if (lexem != null && lexem.getToken().name().equals(token.name())) {
            ++position;

            return lexem;
        }

        throw new ParseException();
    }

    public boolean check(List<Lexem> lexems, Token token) {
        Lexem lexem = get(lexems);

        return lexem!= null && lexem.getToken().equals(token);
    }

    private Lexem get(List<Lexem> lexems) {
        return position >= lexems.size() ? null : lexems.get(position);
    }
}
