package ru.gadjini.reminder.service.parser.time.parser;

import org.joda.time.Period;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class RepeatTimeParser {

    private RepeatTime repeatTime;

    private DayOfWeekService dayOfWeekService;

    private final Locale locale;

    private LexemsConsumer lexemsConsumer;

    public RepeatTimeParser(LexemsConsumer lexemsConsumer, DayOfWeekService dayOfWeekService, Locale locale, ZoneId zoneId) {
        this.dayOfWeekService = dayOfWeekService;
        this.locale = locale;
        this.lexemsConsumer = lexemsConsumer;
        this.repeatTime = new RepeatTime(zoneId);
    }

    public RepeatTime parse(List<BaseLexem> lexems) {
        repeatTime.setInterval(new Period());

        if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAY_OF_WEEK)) {
            consumeDayOfWeek(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MONTHS)) {
            consumeMonths(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAY)) {
            repeatTime.setInterval(repeatTime.getInterval().withYears(1));
            consumeDay(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.YEARS)) {
            consumeYears(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        }

        return repeatTime;
    }

    private void consumeMonths(List<BaseLexem> lexems) {
        int months = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MONTHS).getValue());
        repeatTime.setInterval(repeatTime.getInterval().withMonths(months));
        consumeDay(lexems);
    }

    private void consumeYears(List<BaseLexem> lexems) {
        int years = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.YEARS).getValue());
        repeatTime.setInterval(repeatTime.getInterval().withYears(years));
        consumeDay(lexems);
    }

    private void consumeDay(List<BaseLexem> lexems) {
        int day = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAY).getValue());
        repeatTime.setDay(day);
        if (lexemsConsumer.check(lexems, TimeToken.MONTH_WORD)) {
            consumeMonthWord(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            repeatTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeMonthWord(List<BaseLexem> lexems) {
        String month = lexemsConsumer.consume(lexems, TimeToken.MONTH_WORD).getValue();
        Month m = Stream.of(Month.values()).filter(item -> item.getDisplayName(TextStyle.FULL, locale).equals(month)).findFirst().orElseThrow(ParseException::new);
        repeatTime.setMonth(m);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            repeatTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeDays(List<BaseLexem> lexems) {
        repeatTime.setInterval(repeatTime.getInterval().withDays(Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAYS).getValue())));
        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            repeatTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeMinutes(List<BaseLexem> lexems) {
        repeatTime.setInterval(repeatTime.getInterval().withMinutes(Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTES).getValue())));
    }

    private void consumeHours(List<BaseLexem> lexems) {
        repeatTime.setInterval(repeatTime.getInterval().withHours(Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOURS).getValue())));
        if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeDayOfWeek(List<BaseLexem> lexems) {
        String dayOfWeekValue = lexemsConsumer.consume(lexems, TimeToken.DAY_OF_WEEK).getValue();
        DayOfWeek dayOfWeek = Stream.of(DayOfWeek.values())
                .filter(dow -> dayOfWeekService.isThatDay(dow, locale, dayOfWeekValue))
                .findFirst()
                .orElseThrow();

        repeatTime.setDayOfWeek(dayOfWeek);
        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            repeatTime.setTime(consumeTime(lexems));
        }
    }

    private LocalTime consumeTime(List<BaseLexem> lexems) {
        int hour = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOUR).getValue());
        int minute = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }
}
