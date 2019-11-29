package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class TimeParser {

    private ZonedDateTime parsedTime;

    private String tomorrow;

    private String dayAfterTomorrow;

    private Locale locale;

    private DayOfWeekService dayOfWeekService;

    private final LexemsConsumer lexemsConsumer;

    public TimeParser(LocalisationService localisationService, Locale locale, LexemsConsumer lexemsConsumer,
                      ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        this.tomorrow = localisationService.getMessage(MessagesProperties.REGEXP_TOMORROW);
        this.dayAfterTomorrow = localisationService.getMessage(MessagesProperties.REGEXP_DAY_AFTER_TOMORROW);
        this.locale = locale;
        this.lexemsConsumer = lexemsConsumer;
        this.parsedTime = ZonedDateTime.now(zoneId);
        this.dayOfWeekService = dayOfWeekService;
    }

    public ZonedDateTime parseTime(List<BaseLexem> lexems) {
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
            if (parsedTime.toLocalDate().equals(LocalDate.now(parsedTime.getZone()))
                    && parsedTime.toLocalTime().isAfter(time)) {
                parsedTime = parsedTime.plusDays(1);
            }
            parsedTime.with(time);
        }

        ZonedDateTime now = ZonedDateTime.now(parsedTime.getZone());
        if (parsedTime.getDayOfMonth() > now.getDayOfMonth()) {
            parsedTime = parsedTime.plusMonths(1);
        }
        if (parsedTime.toLocalDate().equals(LocalDate.now(parsedTime.getZone()))
                && parsedTime.toLocalTime().isAfter(now.toLocalTime())) {
            parsedTime = parsedTime.plusDays(1);
        }

        return parsedTime;
    }

    private void consumeNextWeek(List<BaseLexem> lexems) {
        lexemsConsumer.consume(lexems, TimeToken.NEXT_WEEK);
        parsedTime = parsedTime.plusDays(7);

        consumeDayOfWeek(lexems);
    }

    private void consumeDayOfWeek(List<BaseLexem> lexems) {
        String dayOfWeekValue = lexemsConsumer.consume(lexems, TimeToken.DAY_OF_WEEK).getValue();
        DayOfWeek dayOfWeek = Stream.of(DayOfWeek.values())
                .filter(dow -> dayOfWeekService.isMatchFullPattern(dow, dayOfWeekValue)
                        || dow.getDisplayName(TextStyle.SHORT, locale).equals(dayOfWeekValue))
                .findFirst()
                .orElseThrow();
        parsedTime = parsedTime.with(TemporalAdjusters.next(dayOfWeek));

        consumeTime(lexems);
    }

    private void consumeMonth(List<BaseLexem> lexems) {
        int month = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MONTH).getValue());
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());

        parsedTime = parsedTime.withMonth(month);

        parsedTime = parsedTime.withDayOfMonth(day);

        LocalTime time = consumeTime(lexems);
        parsedTime = parsedTime.with(time);
    }

    private void consumeMonthWord(List<BaseLexem> lexems) {
        String month = lexemsConsumer.consume(lexems, TimeToken.MONTH_WORD).getValue();

        Month m = Stream.of(Month.values()).filter(item -> item.getDisplayName(TextStyle.FULL, locale).equals(month)).findFirst().orElseThrow(ParseException::new);

        parsedTime = parsedTime.withMonth(m.getValue());
        parsedTime = parsedTime.with(consumeTime(lexems));
    }

    private void consumeDayWord(List<BaseLexem> lexems) {
        String dayWord = lexemsConsumer.consume(lexems, TimeToken.DAY_WORD).getValue();

        if (dayWord.equals(tomorrow)) {
            parsedTime = parsedTime.plusDays(1);
            parsedTime = parsedTime.with(consumeTime(lexems));
        } else if (dayWord.equals(dayAfterTomorrow)) {
            parsedTime = parsedTime.plusDays(2);
            parsedTime = parsedTime.with(consumeTime(lexems));
        }
    }

    private void consumeDay(List<BaseLexem> lexems) {
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());

        parsedTime.withDayOfMonth(day);
        if (lexemsConsumer.check(lexems, TimeToken.MONTH_WORD)) {
            consumeMonthWord(lexems);
        } else {
            parsedTime.with(consumeTime(lexems));
        }
        if (parsedTime.getDayOfMonth() > day) {
            parsedTime = parsedTime.plusMonths(1);
        }
    }

    private LocalTime consumeTime(List<BaseLexem> lexems) {
        int hour = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOUR).getValue());
        int minute = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }
}
