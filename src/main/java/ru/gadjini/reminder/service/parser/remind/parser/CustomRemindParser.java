package ru.gadjini.reminder.service.parser.remind.parser;

import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindToken;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.service.parser.time.parser.RepeatTimeParser;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public class CustomRemindParser {

    private CustomRemindTime customRemindTime = new CustomRemindTime();

    private ParsedOffsetTime parsedOffsetTime = null;

    private DateTime parsedTime = null;

    private RepeatTime parsedRepeatTime = null;

    private TimeParser timeParser;

    private RepeatTimeParser repeatTimeParser;

    private String typeBefore;

    private String typeAfter;

    private LexemsConsumer lexemsConsumer;

    public CustomRemindParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        this.typeBefore = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_BEFORE);
        this.typeAfter = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_AFTER);
        this.lexemsConsumer = new LexemsConsumer();
        this.timeParser = new TimeParser(localisationService, locale, lexemsConsumer, zoneId, dayOfWeekService);
        this.repeatTimeParser = new RepeatTimeParser(lexemsConsumer, dayOfWeekService, locale);
    }

    public CustomRemindTime parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.REPEAT)) {
            lexemsConsumer.consume(lexems, TimeToken.REPEAT);
            consumeRepeatTime(lexems);
        } else {
            consumeStandardTime(lexems);
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }
        customRemindTime.setParsedOffsetTime(parsedOffsetTime);
        customRemindTime.setRepeatTime(parsedRepeatTime);
        customRemindTime.setTime(parsedTime);

        return customRemindTime;
    }

    private void consumeRepeatTime(List<BaseLexem> lexems) {
        parsedRepeatTime = repeatTimeParser.parse(lexems);
    }

    private void consumeStandardTime(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, CustomRemindToken.TYPE)) {
            parsedOffsetTime = new ParsedOffsetTime();
            consumeType(lexems);
        } else {
            parsedTime = timeParser.parseTime(lexems);
        }
    }

    private void consumeType(List<BaseLexem> lexems) {
        String type = lexemsConsumer.consume(lexems, CustomRemindToken.TYPE).getValue();
        if (type.equals(typeAfter)) {
            parsedOffsetTime.setType(ParsedOffsetTime.Type.AFTER);
        } else if (type.equals(typeBefore)) {
            parsedOffsetTime.setType(ParsedOffsetTime.Type.BEFORE);
        } else {
            throw new ParseException();
        }

        if (lexemsConsumer.check(lexems, CustomRemindToken.HOUR)) {
            int hour = Integer.parseInt(lexemsConsumer.consume(lexems, CustomRemindToken.HOUR).getValue());
            parsedOffsetTime.setHour(hour);
        }
        if (lexemsConsumer.check(lexems, CustomRemindToken.MINUTE)) {
            int minute = Integer.parseInt(lexemsConsumer.consume(lexems, CustomRemindToken.MINUTE).getValue());
            parsedOffsetTime.setMinute(minute);
        }
    }
}
