package ru.gadjini.reminder.service.parser.reminder.parser;

import org.joda.time.Period;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderToken;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ReminderRequestParser {

    private ParsedRequest parsedRequest = new ParsedRequest();

    private ZonedDateTime parsedTime = null;

    private RepeatTime repeatTime = null;

    private TimeParser timeParser;

    private LexemsConsumer lexemsConsumer;

    private Locale locale;

    private DayOfWeekService dayOfWeekService;

    public ReminderRequestParser(LocalisationService localisationService,
                                 Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        this.locale = locale;
        this.dayOfWeekService = dayOfWeekService;
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
            throw new ParseException();
        }

        throw new ParseException();
    }

    public ZonedDateTime parseTime(List<BaseLexem> lexems) {
        consumeFullTime(lexems);

        if (parsedTime == null) {
            throw new ParseException();
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return parsedTime;
    }

    private void consumeRepeatTime(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, ReminderToken.MINUTES)) {
            repeatTime.setInterval(new Period());
            consumeMinutes(lexems);
        } else if (lexemsConsumer.check(lexems, ReminderToken.HOURS)) {
            consumeHours(lexems);
        } else {
            repeatTime.setTime(consumeShortTime(lexems));

            if (lexemsConsumer.check(lexems, ReminderToken.DAY_OF_WEEK)) {
                consumeDayOfWeek(lexems);
            }
        }
    }

    private void consumeMinutes(List<BaseLexem> lexems) {
        repeatTime.getInterval().withMinutes(Integer.parseInt(lexemsConsumer.consume(lexems, ReminderToken.MINUTES).getValue()));
    }

    private void consumeHours(List<BaseLexem> lexems) {
        repeatTime.getInterval().withHours(Integer.parseInt(lexemsConsumer.consume(lexems, ReminderToken.HOURS).getValue()));
        consumeMinutes(lexems);
    }

    private void consumeDayOfWeek(List<BaseLexem> lexems) {
        String dayOfWeekValue = lexemsConsumer.consume(lexems, ReminderToken.DAY_OF_WEEK).getValue();
        DayOfWeek dayOfWeek = Stream.of(DayOfWeek.values())
                .filter(dow -> dayOfWeekService.isThatDay(dow, locale, dayOfWeekValue))
                .findFirst()
                .orElseThrow();

        repeatTime.setDayOfWeek(dayOfWeek);
        consumeShortTime(lexems);
    }

    private void consumeText(List<BaseLexem> lexems) {
        parsedRequest.setText(lexemsConsumer.consume(lexems, ReminderToken.TEXT).getValue());

        if (lexemsConsumer.check(lexems, ReminderToken.REPEAT)) {
            repeatTime = new RepeatTime();
            consumeRepeatTime(lexems);
        } else {
            consumeFullTime(lexems);
        }
        if (lexemsConsumer.check(lexems, ReminderToken.NOTE)) {
            parsedRequest.setNote(lexemsConsumer.consume(lexems, ReminderToken.NOTE).getValue());
        }
    }

    private void consumeFullTime(List<BaseLexem> lexems) {
        parsedTime = timeParser.parseTime(lexems);
    }

    private LocalTime consumeShortTime(List<BaseLexem> lexems) {
        int hour = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOUR).getValue());
        int minute = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }

    private void consumeLogin(List<BaseLexem> lexems) {
        parsedRequest.setReceiverName(lexemsConsumer.consume(lexems, ReminderToken.LOGIN).getValue());

        consumeText(lexems);
    }
}
