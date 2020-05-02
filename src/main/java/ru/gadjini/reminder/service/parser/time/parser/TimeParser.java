package ru.gadjini.reminder.service.parser.time.parser;

import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.api.LexemsConsumer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public class TimeParser {

    private Time time;

    private FixedTimeParser fixedTimeParser;

    private RepeatTimeParser repeatTimeParser;

    private OffsetTimeParser offsetTimeParser;

    private LexemsConsumer lexemsConsumer;

    public TimeParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService, TimeCreator timeCreator, LexemsConsumer lexemsConsumer) {
        this.lexemsConsumer = lexemsConsumer;
        this.fixedTimeParser = new FixedTimeParser(localisationService, locale, lexemsConsumer, zoneId, dayOfWeekService, timeCreator);
        this.repeatTimeParser = new RepeatTimeParser(lexemsConsumer, dayOfWeekService, locale, zoneId);
        this.offsetTimeParser = new OffsetTimeParser(localisationService, dayOfWeekService, locale, zoneId, lexemsConsumer);
        this.time = new Time(zoneId);
    }

    public TimeParser(LocalisationService localisationService, Locale locale, ZoneId zoneId, DayOfWeekService dayOfWeekService, TimeCreator timeCreator) {
        this(localisationService, locale, zoneId, dayOfWeekService, timeCreator, new LexemsConsumer());
    }

    public Time parseWithParseException(List<Lexem> lexems) {
        parse(lexems);

        if (lexemsConsumer.getPosition() < lexems.size()) {
            throw new ParseException();
        }

        return time;
    }

    public Time parse(List<Lexem> lexems) {
        if (lexemsConsumer.check(lexems, TimeToken.REPEAT)) {
            lexemsConsumer.consume(lexems, TimeToken.REPEAT);
            time.setRepeatTimes(repeatTimeParser.parse(lexems));
        } else if (lexemsConsumer.check(lexems, TimeToken.OFFSET)) {
            lexemsConsumer.consume(lexems, TimeToken.OFFSET);
            time.setOffsetTime(offsetTimeParser.parse(lexems));
        } else {
            time.setFixedTime(fixedTimeParser.parse(lexems));
        }

        return time;
    }
}
