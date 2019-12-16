package ru.gadjini.reminder.service.parser.time.parser;

import org.joda.time.Period;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class RepeatTimeParser {

    private RepeatTime repeatTime = new RepeatTime();

    private DayOfWeekService dayOfWeekService;

    private final Locale locale;

    private LexemsConsumer lexemsConsumer;

    public RepeatTimeParser(LexemsConsumer lexemsConsumer, DayOfWeekService dayOfWeekService, Locale locale) {
        this.dayOfWeekService = dayOfWeekService;
        this.locale = locale;
        this.lexemsConsumer = lexemsConsumer;
    }

    public RepeatTime parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            repeatTime.setInterval(new Period());
            consumeMinutes(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            repeatTime.setInterval(new Period());
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAY_OF_WEEK)) {
            consumeDayOfWeek(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.EVERY_DAY)) {
            repeatTime.setInterval(new Period());
            consumeEveryDay(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.EVERY_MINUTE)) {
            repeatTime.setInterval(new Period());
            consumeEveryMinute(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.EVERY_HOUR)) {
            repeatTime.setInterval(new Period());
            consumeEveryHour(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            repeatTime.setInterval(new Period());
            consumeDays(lexems);
        }

        return repeatTime;
    }

    private void consumeEveryMinute(List<BaseLexem> lexems) {
        lexemsConsumer.consume(lexems, TimeToken.EVERY_MINUTE);
        repeatTime.setInterval(repeatTime.getInterval().withMinutes(1));
    }

    private void consumeEveryHour(List<BaseLexem> lexems) {
        lexemsConsumer.consume(lexems, TimeToken.EVERY_HOUR);
        repeatTime.setInterval(repeatTime.getInterval().withHours(1));
        if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeEveryDay(List<BaseLexem> lexems) {
        lexemsConsumer.consume(lexems, TimeToken.EVERY_DAY);
        repeatTime.setInterval(repeatTime.getInterval().withDays(1));
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
