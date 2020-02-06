package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("CPD-START")
public class OffsetTimeParser {

    private String typeBefore;

    private String typeAfter;

    private String typeOn;

    private String eve;

    private OffsetTime offsetTime;

    private LexemsConsumer lexemsConsumer;

    public OffsetTimeParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, LexemsConsumer lexemsConsumer) {
        this.typeBefore = localisationService.getCurrentLocaleMessage(MessagesProperties.OFFSET_TIME_TYPE_BEFORE, locale);
        this.typeAfter = localisationService.getCurrentLocaleMessage(MessagesProperties.OFFSET_TIME_TYPE_AFTER, locale);
        this.typeOn = localisationService.getCurrentLocaleMessage(MessagesProperties.OFFSET_TIME_TYPE_FOR, locale);
        this.eve = localisationService.getCurrentLocaleMessage(MessagesProperties.EVE, locale);
        this.lexemsConsumer = lexemsConsumer;
        this.offsetTime = new OffsetTime(zoneId);
    }

    public OffsetTime parse(List<BaseLexem> lexems) {
        consumeType(lexems);

        return offsetTime;
    }

    private void consumeType(List<BaseLexem> lexems) {
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

    private void consumeEveType(List<BaseLexem> lexems) {
        offsetTime.setDays(1);
        offsetTime.setTime(consumeTime(lexems));
    }

    private void consumePeriod(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.YEARS)) {
            consumeYears(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MONTHS)) {
            consumeMonths(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeDays(List<BaseLexem> lexems) {
        int days = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.DAYS).getValue());
        offsetTime.setDays(days);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeYears(List<BaseLexem> lexems) {
        int years = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.YEARS).getValue());
        offsetTime.setYears(years);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        } else if (lexemsConsumer.check(lexems, TimeToken.MONTHS)) {
            consumeMonths(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeMonths(List<BaseLexem> lexems) {
        int months = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MONTHS).getValue());
        offsetTime.setMonths(months);

        if (lexemsConsumer.check(lexems, TimeToken.HOUR)) {
            offsetTime.setTime(consumeTime(lexems));
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.HOURS)) {
            consumeHours(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeHours(List<BaseLexem> lexems) {
        int hours = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOURS).getValue());
        offsetTime.setHours(hours);

        if (lexemsConsumer.check(lexems, TimeToken.MINUTES)) {
            consumeMinutes(lexems);
        }
    }

    private void consumeMinutes(List<BaseLexem> lexems) {
        int minutes = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTES).getValue());
        offsetTime.setMinutes(minutes);
    }

    private LocalTime consumeTime(List<BaseLexem> lexems) {
        int hour = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.HOUR).getValue());
        int minute = Integer.parseInt(lexemsConsumer.consume(lexems, TimeToken.MINUTE).getValue());

        return LocalTime.of(hour, minute);
    }
}
