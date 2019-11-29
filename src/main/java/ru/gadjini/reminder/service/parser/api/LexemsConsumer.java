package ru.gadjini.reminder.service.parser.api;

import ru.gadjini.reminder.exception.ParseException;

import java.util.List;

public class LexemsConsumer {

    private int position;

    public int getPosition() {
        return position;
    }

    public BaseLexem consume(List<BaseLexem> lexems, Token token) {
        BaseLexem lexem = get(lexems);

        if (lexem != null && lexem.getToken().name().equals(token.name())) {
            ++position;

            return lexem;
        }

        throw new ParseException();
    }

    public boolean check(List<BaseLexem> lexems, Token token) {
        BaseLexem lexem = get(lexems);

        return lexem!= null && lexem.getToken().equals(token);
    }

    private BaseLexem get(List<BaseLexem> lexems) {
        return position >= lexems.size() ? null : lexems.get(position);
    }
}
