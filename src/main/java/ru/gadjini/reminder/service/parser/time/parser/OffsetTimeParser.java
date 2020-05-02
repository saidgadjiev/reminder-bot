package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@SuppressWarnings("CPD-START")
public class OffsetTimeParser {

    private final DayOfWeekService dayOfWeekService;

    private final Locale locale;

    private String typeBefore;

    private String typeAfter;

    private String typeOn;

    private String eve;

    private OffsetTime offsetTime;

    private LexemsConsumer lexemsConsumer;

    public OffsetTimeParser(LocalisationService localisationService, DayOfWeekService dayOfWeekService,
                            Locale locale, ZoneId zoneId, LexemsConsumer lexemsConsumer) {
        this.locale = locale;
        this.typeBefore = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_BEFORE, locale);
        this.typeAfter = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_AFTER, locale);
        this.typeOn = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_FOR, locale);
        this.eve = localisationService.getMessage(MessagesProperties.EVE, locale);
        this.lexemsConsumer = lexemsConsumer;
        this.dayOfWeekService = dayOfWeekService;
        this.offsetTime = new OffsetTime(zoneId);
    }

    public OffsetTime parse(List<Lexem> lexems) {
        consumeType(lexems);

        return offsetTime;
    }

    private void consumeType(List<Lexem> lexems) {
        String type = lexemsConsumer.consume(lexems, TimeToken.TYPE).getValue();
        if (typeAfter.equals(type)) {
            offsetTime.setType(OffsetTime.Type.AFTER);
            consumePeriod(lexems);
        } else if (type.equals(typeBefore)) {
            offsetTime.setType(OffsetTime.Type.BEFORE);
            consumePeriod(lexems);
        } else if (type.equals(typeOn)) {
            offsetTime.setType(OffsetTime.Type.FOR);
            consumePeriod(lexems);
        } else if (type.equals(eve)) {
            offsetTime.setType(OffsetTime.Type.BEFORE);
            consumeEveType(lexems);
        }
    }

    private void consumeEveType(List<Lexem> lexems) {
        offsetTime.setDays(1);
        offsetTime.setTime(consumeTime(lexems));
    }

    private void consumePeriod(List<Lexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.YEARS)) {
            consumeYears(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MONTHS)) {
            consumeMonths(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.WEEKS)) {
            consumeWeeks(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeWeeks(List<Lexem> lexems) {
        int weeks = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.WEEKS).getValue());
        offsetTime.setWeeks(weeks);

        if (lexemsConsumer.check(lexems, TimeToken.DAY_OF_WEEK)) {
            consumeDayOfWeek(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeDayOfWeek(List<Lexem> lexems) {
        String dayOfWeekValue = lexemsConsumer.consume(lexems, TimeToken.DAY_OF_WEEK).getValue();
        DayOfWeek dayOfWeek = Stream.of(DayOfWeek.values())
                .filter(dow -> dayOfWeekService.isThatDay(dow, dayOfWeekValue, locale))
                .findFirst()
                .orElseThrow();

        offsetTime.setDayOfWeek(dayOfWeek);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeDays(List<Lexem> lexems) {
        int days = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAYS).getValue());
        offsetTime.setWeeks(offsetTime.getWeeks() + days / 7);
        offsetTime.setDays(days % 7);

        if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeYears(List<Lexem> lexems) {
        int years = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.YEARS).getValue());
        offsetTime.setYears(years);

        if (lexemsConsumer.check(lexems, TimeToken.MONTHS)) {
            consumeMonths(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.WEEKS)) {
            consumeWeeks(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeMonths(List<Lexem> lexems) {
        int months = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MONTHS).getValue());
        offsetTime.setMonths(months);

        if (lexemsConsumer.check(lexems, TimeToken.WEEKS)) {
            consumeWeeks(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        }
    }

    private void consumeHours(List<Lexem> lexems) {
        int hours = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOURS).getValue());
        offsetTime.setHours(hours);

        if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeMinutes(List<Lexem> lexems) {
        int minutes = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTES).getValue());
        offsetTime.setMinutes(minutes);
    }

    private LocalTime consumeTime(List<Lexem> lexems) {
        int hour = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOUR).getValue());
        int minute = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }
}
