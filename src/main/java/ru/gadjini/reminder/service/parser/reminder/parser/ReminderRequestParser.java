package ru.gadjini.reminder.service.parser.reminder.parser;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderToken;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public class ReminderRequestParser {

    private ReminderRequest reminderRequest = new ReminderRequest();

    private TimeParser timeParser;

    private LexemsConsumer lexemsConsumer;

    public ReminderRequestParser(LocalisationService localisationService,
                                 Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        lexemsConsumer = new LexemsConsumer();
        timeParser = new TimeParser(localisationService, locale, zoneId, dayOfWeekService, lexemsConsumer);
        reminderRequest.setTime(new Time(zoneId));
    }

    public ReminderRequest parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, ReminderToken.TEXT)) {
            consumeText(lexems);
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return reminderRequest;
    }

    private void consumeText(List<BaseLexem> lexems) {
        String text = lexemsConsumer.consume(lexems, ReminderToken.TEXT).getValue();

        if (StringUtils.isBlank(text)) {
            throw new ParseException();
        }
        reminderRequest.setText(text);

        reminderRequest.setTime(timeParser.parse(lexems));
        if (lexemsConsumer.check(lexems, ReminderToken.NOTE)) {
            reminderRequest.setNote(lexemsConsumer.consume(lexems, ReminderToken.NOTE).getValue());
        }
    }
}
