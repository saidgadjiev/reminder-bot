package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.domain.Time;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.service.parser.time.parser.OffsetTimeParser;
import ru.gadjini.reminder.service.parser.time.parser.RepeatTimeParser;
import ru.gadjini.reminder.service.parser.time.parser.FixedTimeParser;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public class TimeParser {

    private Time time;

    private FixedTimeParser fixedTimeParser;

    private RepeatTimeParser repeatTimeParser;

    private OffsetTimeParser offsetTimeParser;

    private LexemsConsumer lexemsConsumer;

    public TimeParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService, LexemsConsumer lexemsConsumer) {
        this.lexemsConsumer = lexemsConsumer;
        this.fixedTimeParser = new FixedTimeParser(localisationService, locale, lexemsConsumer, zoneId, dayOfWeekService);
        this.repeatTimeParser = new RepeatTimeParser(lexemsConsumer, dayOfWeekService, locale, zoneId);
        this.offsetTimeParser = new OffsetTimeParser(localisationService, zoneId, lexemsConsumer);
        this.time = new Time(zoneId);
    }

    public TimeParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService) {
        this(localisationService, locale, zoneId, dayOfWeekService, new LexemsConsumer());
    }

    public Time parseWithParseException(List<BaseLexem> lexems) {
        parse(lexems);

        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return time;
    }

    public Time parse(List<BaseLexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.REPEAT)) {
            lexemsConsumer.consume(lexems, TimeToken.REPEAT);
            time.setRepeatTime(repeatTimeParser.parse(lexems));
        } else if (lexemsConsumer.check(lexems, TimeToken.OFFSET)) {
            lexemsConsumer.consume(lexems, TimeToken.OFFSET);
            time.setOffsetTime(offsetTimeParser.parse(lexems));
        } else {
            time.setFixedTime(fixedTimeParser.parse(lexems));
        }

        return time;
    }
}
