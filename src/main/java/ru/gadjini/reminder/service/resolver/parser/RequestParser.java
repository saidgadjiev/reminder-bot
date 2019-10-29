package ru.gadjini.reminder.service.resolver.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.resolver.lexer.Lexem;
import ru.gadjini.reminder.service.resolver.lexer.Token;

import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class RequestParser {

    private String tomorrow;

    private String dayAfterTomorrow;

    private Locale locale;

    private ParsedRequest parsedRequest = new ParsedRequest();

    private ParsedTime parsedTime = new ParsedTime();

    private int position;

    public RequestParser(LocalisationService localisationService, Locale locale) {
        this.tomorrow = localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TOMORROW);
        this.dayAfterTomorrow = localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW);
        this.locale = locale;
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
        if (check(lexems, Token.MONTH)) {
            consumeMonth(lexems);

            return parsedTime;
        } else if (check(lexems, Token.DAYWORD)) {
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

    private void consumeMonth(List<Lexem> lexems) {
        int month = Integer.parseInt(consume(lexems, Token.MONTH).getValue());
        int day = Integer.parseInt(consume(lexems, Token.DAY).getValue());

        parsedTime.setMonth(month);
        parsedTime.setDay(day);
        parsedTime.setTime(consumeTime(lexems));
    }

    private void consumeMonthWord(List<Lexem> lexems) {
        String month = consume(lexems, Token.MONTHWORD).getValue();

        Month m = Stream.of(Month.values()).filter(item -> item.getDisplayName(TextStyle.FULL, locale).equals(month)).findFirst().orElseThrow(ParseException::new);

        parsedTime.setMonth(m.getValue());
        parsedTime.setTime(consumeTime(lexems));
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
        if (check(lexems, Token.MONTHWORD)) {
            consumeMonthWord(lexems);
        } else {
            parsedTime.setTime(consumeTime(lexems));
        }
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
