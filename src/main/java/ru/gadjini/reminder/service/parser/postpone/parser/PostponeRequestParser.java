package ru.gadjini.reminder.service.parser.postpone.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.postpone.lexer.PostponeToken;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

public class PostponeRequestParser {

    private ParsedPostponeTime postponeTime = new ParsedPostponeTime();

    private String typeOn;

    private String typeAt;

    private TimeParser timeParser;

    private LexemsConsumer lexemsConsumer;

    public PostponeRequestParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        typeOn = localisationService.getMessage(MessagesProperties.REGEX_POSTPONE_TYPE_ON);
        typeAt = localisationService.getMessage(MessagesProperties.REGEX_POSTPONE_TYPE_AT);
        lexemsConsumer = new LexemsConsumer();
        timeParser = new TimeParser(localisationService, locale, lexemsConsumer, zoneId, dayOfWeekService);
    }

    public ParsedPostponeTime parse(List<BaseLexem> lexems) {
        String type = lexemsConsumer.consume(lexems, PostponeToken.TYPE).getValue();
        if (type.equals(typeOn)) {
            postponeTime.setPostponeOn(new PostponeOn());
            consumeTypeOn(lexems);
        } else if (type.equals(typeAt)) {
            ZonedDateTime parsedTime = timeParser.parseTime(lexems);
            postponeTime.setPostponeAt(parsedTime);
        } else {
            throw new ParseException();
        }

        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return postponeTime;
    }

    private void consumeTypeOn(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, PostponeToken.ON_DAY)) {
            int day = Integer.parseInt(lexemsConsumer.consume(lexems, PostponeToken.ON_DAY).getValue());

            postponeTime.getPostponeOn().setDay(day);
        }
        if (lexemsConsumer.check(lexems, PostponeToken.ON_HOUR)) {
            int hour = Integer.parseInt(lexemsConsumer.consume(lexems, PostponeToken.ON_HOUR).getValue());

            postponeTime.getPostponeOn().setHour(hour);
        }
        if (lexemsConsumer.check(lexems, PostponeToken.ON_MINUTE)) {
            int minute = Integer.parseInt(lexemsConsumer.consume(lexems, PostponeToken.ON_MINUTE).getValue());

            postponeTime.getPostponeOn().setMinute(minute);
        }
    }
}
