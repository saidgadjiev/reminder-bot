package ru.gadjini.reminder.service.requestresolver.reminder.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.requestresolver.reminder.lexer.ReminderLexem;
import ru.gadjini.reminder.service.requestresolver.reminder.lexer.ReminderToken;

import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ReminderRequestParser {

    private String tomorrow;

    private String dayAfterTomorrow;

    private Locale locale;

    private ParsedRequest parsedRequest = new ParsedRequest();

    private ParsedTime parsedTime = new ParsedTime();

    private int position;

    public ReminderRequestParser(LocalisationService localisationService, Locale locale) {
        this.tomorrow = localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TOMORROW);
        this.dayAfterTomorrow = localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW);
        this.locale = locale;
    }

    public ParsedRequest parse(List<ReminderLexem> lexems) {
        if (check(lexems, ReminderToken.LOGIN)) {
            consumeLogin(lexems);

            parsedRequest.setParsedTime(parsedTime);

            return parsedRequest;
        } else if (check(lexems, ReminderToken.TEXT)) {
            consumeText(lexems);
            parsedRequest.setParsedTime(parsedTime);

            return parsedRequest;
        }
        if (position < lexems.size()) {
            throw new ParseException();
        }

        throw new ParseException();
    }

    public ParsedTime parseTime(List<ReminderLexem> lexems) {
        if (check(lexems, ReminderToken.MONTH)) {
            consumeMonth(lexems);

            return parsedTime;
        } else if (check(lexems, ReminderToken.DAYWORD)) {
            consumeDayWord(lexems);

            return parsedTime;
        } else if (check(lexems, ReminderToken.DAY)) {
            consumeDay(lexems);

            return parsedTime;
        } else if (check(lexems, ReminderToken.HOUR)) {
            parsedTime.setTime(consumeTime(lexems));

            return parsedTime;
        }
        if (position < lexems.size()) {
            throw new ParseException();
        }

        throw new ParseException();
    }

    private void consumeMonth(List<ReminderLexem> lexems) {
        int month = Integer.parseInt(consume(lexems, ReminderToken.MONTH).getValue());
        int day = Integer.parseInt(consume(lexems, ReminderToken.DAY).getValue());

        parsedTime.setMonth(month);
        parsedTime.setDay(day);
        parsedTime.setTime(consumeTime(lexems));
    }

    private void consumeMonthWord(List<ReminderLexem> lexems) {
        String month = consume(lexems, ReminderToken.MONTHWORD).getValue();

        Month m = Stream.of(Month.values()).filter(item -> item.getDisplayName(TextStyle.FULL, locale).equals(month)).findFirst().orElseThrow(ParseException::new);

        parsedTime.setMonth(m.getValue());
        parsedTime.setTime(consumeTime(lexems));
    }

    private void consumeText(List<ReminderLexem> lexems) {
        parsedRequest.setText(consume(lexems, ReminderToken.TEXT).getValue());

        parseTime(lexems);
    }

    private void consumeDayWord(List<ReminderLexem> lexems) {
        String dayWord = consume(lexems, ReminderToken.DAYWORD).getValue();

        if (dayWord.equals(tomorrow)) {
            parsedTime.setAddDays(1);
            parsedTime.setTime(consumeTime(lexems));
        } else if (dayWord.equals(dayAfterTomorrow)) {
            parsedTime.setAddDays(2);
            parsedTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeLogin(List<ReminderLexem> lexems) {
        parsedRequest.setReceiverName(consume(lexems, ReminderToken.LOGIN).getValue());

        consumeText(lexems);
    }

    private void consumeDay(List<ReminderLexem> lexems) {
        int day = Integer.parseInt(consume(lexems, ReminderToken.DAY).getValue());

        parsedTime.setDay(day);
        if (check(lexems, ReminderToken.MONTHWORD)) {
            consumeMonthWord(lexems);
        } else {
            parsedTime.setTime(consumeTime(lexems));
        }
    }

    private LocalTime consumeTime(List<ReminderLexem> lexems) {
        int hour = Integer.parseInt(consume(lexems, ReminderToken.HOUR).getValue());
        int minute = Integer.parseInt(consume(lexems, ReminderToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }

    private ReminderLexem consume(List<ReminderLexem> lexems, ReminderToken token) {
        ReminderLexem lexem = get(lexems);

        if (lexem.getToken().equals(token)) {
            ++position;

            return lexem;
        }

        throw new ParseException();
    }

    private boolean check(List<ReminderLexem> lexems, ReminderToken token) {
        ReminderLexem lexem = get(lexems);

        return lexem.getToken().equals(token);
    }

    private ReminderLexem get(List<ReminderLexem> lexems) {
        return lexems.get(position);
    }
}
