package ru.gadjini.reminder.service.parser.reminder.parser;

import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderToken;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.service.parser.time.parser.OffsetTimeParser;
import ru.gadjini.reminder.service.parser.time.parser.RepeatTimeParser;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public class ReminderRequestParser {

    private ParsedRequest parsedRequest = new ParsedRequest();

    private TimeParser timeParser;

    private RepeatTimeParser repeatTimeParser;

    private OffsetTimeParser offsetTimeParser;

    private LexemsConsumer lexemsConsumer;

    public ReminderRequestParser(LocalisationService localisationService,
                                 Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        lexemsConsumer = new LexemsConsumer();
        timeParser = new TimeParser(localisationService, locale, lexemsConsumer, zoneId, dayOfWeekService);
        repeatTimeParser = new RepeatTimeParser(lexemsConsumer, dayOfWeekService, locale, zoneId);
        offsetTimeParser = new OffsetTimeParser(localisationService, zoneId, lexemsConsumer);
    }

    public ParsedRequest parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, ReminderToken.LOGIN)) {
            consumeLogin(lexems);
        } else if (lexemsConsumer.check(lexems, ReminderToken.TEXT)) {
            consumeText(lexems);
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return parsedRequest;
    }

    public DateTime parseTime(List<BaseLexem> lexems) {
        DateTime parsedTime = timeParser.parse(lexems);

        if (parsedTime == null) {
            throw new ParseException();
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return parsedTime;
    }

    private void consumeText(List<BaseLexem> lexems) {
        parsedRequest.setText(lexemsConsumer.consume(lexems, ReminderToken.TEXT).getValue());

        if (lexemsConsumer.check(lexems, TimeToken.REPEAT)) {
            lexemsConsumer.consume(lexems, TimeToken.REPEAT);
            parsedRequest.setRepeatTime(repeatTimeParser.parse(lexems));
        } else if (lexemsConsumer.check(lexems, TimeToken.OFFSET)) {
            parsedRequest.setOffsetTime(offsetTimeParser.parse(lexems));
        } else {
            parsedRequest.setParsedTime(timeParser.parse(lexems));
        }
        if (lexemsConsumer.check(lexems, ReminderToken.NOTE)) {
            parsedRequest.setNote(lexemsConsumer.consume(lexems, ReminderToken.NOTE).getValue());
        }
    }

    private void consumeLogin(List<BaseLexem> lexems) {
        parsedRequest.setReceiverName(lexemsConsumer.consume(lexems, ReminderToken.LOGIN).getValue());

        consumeText(lexems);
    }
}
