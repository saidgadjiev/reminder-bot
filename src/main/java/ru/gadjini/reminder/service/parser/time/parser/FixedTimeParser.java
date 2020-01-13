package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class FixedTimeParser {

    private FixedTime fixedTime;

    private String tomorrow;

    private String dayAfterTomorrow;

    private String typeUntil;

    private String typeAt;

    private Locale locale;

    private DayOfWeekService dayOfWeekService;

    private final LexemsConsumer lexemsConsumer;

    public FixedTimeParser(LocalisationService localisationService, Locale locale, LexemsConsumer lexemsConsumer,
                           ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        this.tomorrow = localisationService.getMessage(MessagesProperties.TOMORROW);
        this.dayAfterTomorrow = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW);
        this.typeUntil = localisationService.getMessage(MessagesProperties.FIXED_TIME_TYPE_UNTIL);
        this.typeAt = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        this.locale = locale;
        this.lexemsConsumer = lexemsConsumer;
        this.fixedTime = new FixedTime();
        this.fixedTime.setDateTime(DateTime.now(zoneId).time(null));
        this.dayOfWeekService = dayOfWeekService;
    }

    public FixedTime parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.TYPE)) {
            consumeType(lexems);
        }
        if (lexemsConsumer.check(lexems, TimeToken.YEAR)) {
            consumeYear(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MONTH)) {
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
            fixedTime.time(time);
        } else {
            fixedTime.time(LocalTime.now(fixedTime.getZone()));
        }

        ZonedDateTime now = TimeUtils.now(fixedTime.getZone());
        if (fixedTime.date().isBefore(now.toLocalDate())) {
            fixedTime.year(now.getYear() + 1);
        }
        if (fixedTime.hasTime() && fixedTime.date().equals(LocalDate.now(fixedTime.getZone())) && now.toLocalTime().isAfter(fixedTime.time())) {
            fixedTime.plusDays(1);
        }

        return fixedTime;
    }

    private void consumeType(List<BaseLexem> lexems) {
        String type = lexemsConsumer.consume(lexems, TimeToken.TYPE).getValue();
        if (type.equals(typeUntil)) {
            fixedTime.setType(FixedTime.Type.UNTIL);
        } else if (type.equals(typeAt)) {
            fixedTime.setType(FixedTime.Type.AT);
        } else {
            throw new ParseException();
        }
    }

    private void consumeNextWeek(List<BaseLexem> lexems) {
        lexemsConsumer.consume(lexems, TimeToken.NEXT_WEEK);

        consumeDayOfWeek(lexems);
        fixedTime.plusDays(7);
    }

    private void consumeDayOfWeek(List<BaseLexem> lexems) {
        String dayOfWeekValue = lexemsConsumer.consume(lexems, TimeToken.DAY_OF_WEEK).getValue();
        DayOfWeek dayOfWeek = Stream.of(DayOfWeek.values())
                .filter(dow -> dayOfWeekService.isThatDay(dow, locale, dayOfWeekValue))
                .findFirst()
                .orElseThrow();
        LocalDate dayOfWeekDate = (LocalDate) TemporalAdjusters.next(dayOfWeek).adjustInto(fixedTime.date());

        fixedTime.date(dayOfWeekDate);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            fixedTime.time(consumeTime(lexems));
        }
    }

    private void consumeYear(List<BaseLexem> lexems) {
        int year = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.YEAR).getValue());
        int month = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MONTH).getValue());
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());

        fixedTime.year(year);
        fixedTime.month(month);
        fixedTime.dayOfMonth(day);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            fixedTime.time(consumeTime(lexems));
        }
    }

    private void consumeMonth(List<BaseLexem> lexems) {
        int month = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MONTH).getValue());
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());

        fixedTime.month(month);

        fixedTime.dayOfMonth(day);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            fixedTime.time(consumeTime(lexems));
        }
    }

    private void consumeMonthWord(List<BaseLexem> lexems) {
        String month = lexemsConsumer.consume(lexems, TimeToken.MONTH_WORD).getValue();

        Month m = Stream.of(Month.values()).filter(item -> item.getDisplayName(TextStyle.FULL, locale).equals(month)).findFirst().orElseThrow(ParseException::new);

        fixedTime.month(m.getValue());
        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            fixedTime.time(consumeTime(lexems));
        }
    }

    private void consumeDayWord(List<BaseLexem> lexems) {
        String dayWord = lexemsConsumer.consume(lexems, TimeToken.DAY_WORD).getValue();

        if (dayWord.equals(tomorrow)) {
            fixedTime.plusDays(1);
        } else if (dayWord.equals(dayAfterTomorrow)) {
            fixedTime.plusDays(2);
        }
        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            fixedTime.time(consumeTime(lexems));
        }
    }

    private void consumeDay(List<BaseLexem> lexems) {
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());

        fixedTime.dayOfMonth(day);
        if (lexemsConsumer.check(lexems, TimeToken.MONTH_WORD)) {
            consumeMonthWord(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            fixedTime.time(consumeTime(lexems));
        }
        if (fixedTime.dayOfMonth() > day) {
            fixedTime.plusMonths(1);
        }
    }

    private LocalTime consumeTime(List<BaseLexem> lexems) {
        int hour = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOUR).getValue());
        int minute = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }
}
