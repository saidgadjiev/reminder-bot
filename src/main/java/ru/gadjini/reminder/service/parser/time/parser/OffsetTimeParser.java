package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.OffsetTime;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindToken;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class OffsetTimeParser {

    private String typeBefore;

    private String typeAfter;

    private OffsetTime offsetTime;

    private LexemsConsumer lexemsConsumer;

    public OffsetTimeParser(LocalisationService localisationService, ZoneId zoneId, LexemsConsumer lexemsConsumer) {
        this.typeBefore = localisationService.getMessage(MessagesProperties.CUSTOM_REMIND_BEFORE);
        this.typeAfter = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_AFTER);
        this.lexemsConsumer = lexemsConsumer;
        this.offsetTime = new OffsetTime(zoneId);
    }

    public OffsetTime parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, CustomRemindToken.TYPE)) {
            consumeType(lexems);
        } else if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
            consumeDays(lexems);
        }

        return offsetTime;
    }

    private void consumeType(List<BaseLexem> lexems) {
        String type = lexemsConsumer.consume(lexems, CustomRemindToken.TYPE).getValue();
        if (type.equals(typeAfter)) {
            offsetTime.setType(OffsetTime.Type.AFTER);
        } else if (type.equals(typeBefore)) {
            offsetTime.setType(OffsetTime.Type.BEFORE);
        }

        if (lexemsConsumer.check(lexems, TimeToken.DAYS)) {
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
