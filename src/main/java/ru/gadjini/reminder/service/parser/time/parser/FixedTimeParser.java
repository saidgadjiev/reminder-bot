package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.time.DateTime;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class FixedTimeParser {

    private DateTime parsedTime;

    private String tomorrow;

    private String dayAfterTomorrow;

    private Locale locale;

    private DayOfWeekService dayOfWeekService;

    private final LexemsConsumer lexemsConsumer;

    public FixedTimeParser(LocalisationService localisationService, Locale locale, LexemsConsumer lexemsConsumer,
                           ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        this.tomorrow = localisationService.getMessage(MessagesProperties.TOMORROW);
        this.dayAfterTomorrow = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW);
        this.locale = locale;
        this.lexemsConsumer = lexemsConsumer;
        this.parsedTime = DateTime.now(zoneId).time(null);
        this.dayOfWeekService = dayOfWeekService;
    }

    public DateTime parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.MONTH)) {
            consumeMonth(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAY_WORD)) {
            consumeDayWord(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAY)) {
            consumeDay(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.NEXT_WEEK)) {
            consumeNextWeek(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAY_OF_WEEK)) {
            consumeDayOfWeek(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            LocalTime time = consumeTime(lexems);
            parsedTime.time(time);
        }

        ZonedDateTime now = ZonedDateTime.now(parsedTime.getZone());
        if (parsedTime.date().isBefore(now.toLocalDate())) {
            parsedTime.month(now.getMonthValue() + 1);
        }
        if (parsedTime.hasTime()) {
            if (parsedTime.date().equals(LocalDate.now(parsedTime.getZone()))
                    && now.toLocalTime().isAfter(parsedTime.time())) {
                parsedTime.plusDays(1);
            }
        }

        return parsedTime;
    }

    private void consumeNextWeek(List<BaseLexem> lexems) {
        lexemsConsumer.consume(lexems, TimeToken.NEXT_WEEK);

        consumeDayOfWeek(lexems);
        parsedTime.plusDays(7);
    }

    private void consumeDayOfWeek(List<BaseLexem> lexems) {
        String dayOfWeekValue = lexemsConsumer.consume(lexems, TimeToken.DAY_OF_WEEK).getValue();
        DayOfWeek dayOfWeek = Stream.of(DayOfWeek.values())
                .filter(dow -> dayOfWeekService.isThatDay(dow, locale, dayOfWeekValue))
                .findFirst()
                .orElseThrow();
        LocalDate dayOfWeekDate = (LocalDate) TemporalAdjusters.next(dayOfWeek).adjustInto(parsedTime.date());

        parsedTime.date(dayOfWeekDate);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            parsedTime.time(consumeTime(lexems));
        }
    }

    private void consumeMonth(List<BaseLexem> lexems) {
        int month = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MONTH).getValue());
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());

        parsedTime.month(month);

        parsedTime.dayOfMonth(day);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            parsedTime.time(consumeTime(lexems));
        }
    }

    private void consumeMonthWord(List<BaseLexem> lexems) {
        String month = lexemsConsumer.consume(lexems, TimeToken.MONTH_WORD).getValue();

        Month m = Stream.of(Month.values()).filter(item -> item.getDisplayName(TextStyle.FULL, locale).equals(month)).findFirst().orElseThrow(ParseException::new);

        parsedTime.month(m.getValue());
        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            parsedTime.time(consumeTime(lexems));
        }
    }

    private void consumeDayWord(List<BaseLexem> lexems) {
        String dayWord = lexemsConsumer.consume(lexems, TimeToken.DAY_WORD).getValue();

        if (dayWord.equals(tomorrow)) {
            parsedTime.plusDays(1);
        } else if (dayWord.equals(dayAfterTomorrow)) {
            parsedTime.plusDays(2);
        }
        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            parsedTime.time(consumeTime(lexems));
        }
    }

    private void consumeDay(List<BaseLexem> lexems) {
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());

        parsedTime.dayOfMonth(day);
        if (lexemsConsumer.check(lexems, TimeToken.MONTH_WORD)) {
            consumeMonthWord(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            parsedTime.time(consumeTime(lexems));
        }
        if (parsedTime.dayOfMonth() > day) {
            parsedTime.plusMonths(1);
        }
    }

    private LocalTime consumeTime(List<BaseLexem> lexems) {
        int hour = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOUR).getValue());
        int minute = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }
}
