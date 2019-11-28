package ru.gadjini.reminder.service.parser.reminder.parser;

import ru.gadjini.reminder.exception.UserMessageParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderToken;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

public class ReminderRequestParser {

    private ParsedRequest parsedRequest = new ParsedRequest();

    private ZonedDateTime parsedTime = null;

    private TimeParser timeParser;

    private LexemsConsumer lexemsConsumer;

    public ReminderRequestParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        lexemsConsumer = new LexemsConsumer();
        timeParser = new TimeParser(localisationService, locale, lexemsConsumer, zoneId, dayOfWeekService);
    }

    public ParsedRequest parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, ReminderToken.LOGIN)) {
            consumeLogin(lexems);

            parsedRequest.setParsedTime(parsedTime);

            return parsedRequest;
        } else if (lexemsConsumer.check(lexems, ReminderToken.TEXT)) {
            consumeText(lexems);
            parsedRequest.setParsedTime(parsedTime);

            return parsedRequest;
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new UserMessageParseException();
        }

        throw new UserMessageParseException();
    }

    public ZonedDateTime parseTime(List<BaseLexem> lexems) {
        parsedTime = timeParser.parseTime(lexems);

        if (parsedTime == null) {
            throw new UserMessageParseException();
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new UserMessageParseException();
        }

        return parsedTime;
    }

    private void consumeText(List<BaseLexem> lexems) {
        parsedRequest.setText(lexemsConsumer.consume(lexems, ReminderToken.TEXT).getValue());

        parseTime(lexems);
        if (lexemsConsumer.check(lexems, ReminderToken.NOTE)) {
            parsedRequest.setNote(lexemsConsumer.consume(lexems, ReminderToken.NOTE).getValue());
        }
    }

    private void consumeLogin(List<BaseLexem> lexems) {
        parsedRequest.setReceiverName(lexemsConsumer.consume(lexems, ReminderToken.LOGIN).getValue());

        consumeText(lexems);
    }
}
