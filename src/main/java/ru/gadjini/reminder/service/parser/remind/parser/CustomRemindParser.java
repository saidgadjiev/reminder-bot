package ru.gadjini.reminder.service.parser.remind.parser;

import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.service.parser.time.parser.OffsetTimeParser;
import ru.gadjini.reminder.service.parser.time.parser.RepeatTimeParser;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public class CustomRemindParser {

    private CustomRemindTime customRemindTime = new CustomRemindTime();

    private TimeParser timeParser;

    private RepeatTimeParser repeatTimeParser;

    private OffsetTimeParser offsetTimeParser;

    private LexemsConsumer lexemsConsumer;

    public CustomRemindParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        this.lexemsConsumer = new LexemsConsumer();
        this.timeParser = new TimeParser(localisationService, locale, lexemsConsumer, zoneId, dayOfWeekService);
        this.repeatTimeParser = new RepeatTimeParser(lexemsConsumer, dayOfWeekService, locale, zoneId);
        this.offsetTimeParser = new OffsetTimeParser(localisationService, zoneId, lexemsConsumer);
    }

    public CustomRemindTime parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.REPEAT)) {
            lexemsConsumer.consume(lexems, TimeToken.REPEAT);
            customRemindTime.setRepeatTime(repeatTimeParser.parse(lexems));
        } else if (lexemsConsumer.check(lexems, TimeToken.OFFSET)) {
            lexemsConsumer.consume(lexems, TimeToken.OFFSET);
            customRemindTime.setOffsetTime(offsetTimeParser.parse(lexems));
        } else {
            customRemindTime.setTime(timeParser.parse(lexems));
        }
        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return customRemindTime;
    }
}
