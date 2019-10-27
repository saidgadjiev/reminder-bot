package ru.gadjini.reminder.service.resolver.parser;

import ru.gadjini.reminder.service.resolver.lexer.Lexem;
import ru.gadjini.reminder.service.resolver.lexer.Token;

import java.time.LocalTime;
import java.util.List;

public class RequestParser {

    private String tomorrow;

    private String dayAfterTomorrow;

    private ParsedRequest parsedRequest = new ParsedRequest();

    private ParsedTime parsedTime = new ParsedTime();

    private int position;

    public RequestParser(String tomorrow, String dayAfterTomorrow) {
        this.tomorrow = tomorrow;
        this.dayAfterTomorrow = dayAfterTomorrow;
    }

    public ParsedRequest parse(List<Lexem> lexems) {
        if (check(lexems, Token.LOGIN)) {
            consumeLogin(lexems);

            parsedRequest.setParsedTime(parsedTime);

            return parsedRequest;
        } else if (check(lexems, Token.TEXT)) {
            consumeText(lexems);
            parsedRequest.setParsedTime(parsedTime);

            return parsedRequest;
        }
        if (position < lexems.size()) {
            throw new ParseException();
        }

        throw new ParseException();
    }

    public ParsedTime parseTime(List<Lexem> lexems) {
        if (check(lexems, Token.DAYWORD)) {
            consumeDayWord(lexems);

            return parsedTime;
        } else if (check(lexems, Token.DAY)) {
            consumeDay(lexems);

            return parsedTime;
        } else if (check(lexems, Token.HOUR)) {
            parsedTime.setTime(consumeTime(lexems));

            return parsedTime;
        }
        if (position < lexems.size()) {
            throw new ParseException();
        }

        throw new ParseException();
    }

    private void consumeText(List<Lexem> lexems) {
        parsedRequest.setText(consume(lexems, Token.TEXT).getValue());

        parseTime(lexems);
    }

    private void consumeDayWord(List<Lexem> lexems) {
        String dayWord = consume(lexems, Token.DAYWORD).getValue();

        if (dayWord.equals(tomorrow)) {
            parsedTime.setAddDays(1);
            parsedTime.setTime(consumeTime(lexems));
        } else if (dayWord.equals(dayAfterTomorrow)) {
            parsedTime.setAddDays(2);
            parsedTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeLogin(List<Lexem> lexems) {
        parsedRequest.setReceiverName(consume(lexems, Token.LOGIN).getValue());

        consumeText(lexems);
    }

    private void consumeDay(List<Lexem> lexems) {
        int day = Integer.parseInt(consume(lexems, Token.DAY).getValue());

        parsedTime.setDay(day);
        parsedTime.setTime(consumeTime(lexems));
    }

    private LocalTime consumeTime(List<Lexem> lexems) {
        int hour = Integer.parseInt(consume(lexems, Token.HOUR).getValue());
        int minute = Integer.parseInt(consume(lexems, Token.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }

    private Lexem consume(List<Lexem> lexems, Token token) {
        Lexem lexem = get(lexems);

        if (lexem.getToken().equals(token)) {
            ++position;

            return lexem;
        }

        throw new ParseException();
    }

    private boolean check(List<Lexem> lexems, Token token) {
        Lexem lexem = get(lexems);

        return lexem.getToken().equals(token);
    }

    private Lexem get(List<Lexem> lexems) {
        return lexems.get(position);
    }
}
