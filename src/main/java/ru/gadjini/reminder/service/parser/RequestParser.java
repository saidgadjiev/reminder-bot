package ru.gadjini.reminder.service.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderRequestLexer;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderRequestLexerConfig;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequestParser;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;
import ru.gadjini.reminder.service.parser.time.parser.TimeParser;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
public class RequestParser {

    private LocalisationService localisationService;

    private final ReminderRequestLexerConfig reminderRequestLexerConfig;

    private final TimeLexerConfig timeLexerConfig;

    private DayOfWeekService dayOfWeekService;

    private DateTimeService timeCreator;

    @Autowired
    public RequestParser(LocalisationService localisationService,
                         ReminderRequestLexerConfig reminderRequestLexerConfig,
                         TimeLexerConfig timeLexerConfig,
                         DayOfWeekService dayOfWeekService, DateTimeService timeCreator) {
        this.localisationService = localisationService;
        this.reminderRequestLexerConfig = reminderRequestLexerConfig;
        this.timeLexerConfig = timeLexerConfig;
        this.dayOfWeekService = dayOfWeekService;
        this.timeCreator = timeCreator;
    }

    public ReminderRequest parseRequest(String text, ZoneId zoneId, Locale locale) {
        List<Lexem> lexems = new ReminderRequestLexer(reminderRequestLexerConfig, timeLexerConfig, text, locale).tokenize();

        return new ReminderRequestParser(localisationService, locale, zoneId, dayOfWeekService, timeCreator).parse(lexems);
    }

    public Time parseTime(String time, ZoneId zoneId, Locale locale) {
        List<Lexem> lexems = new TimeLexer(timeLexerConfig, time, true, locale).tokenizeThrowParseException();

        return new TimeParser(localisationService, locale, zoneId, dayOfWeekService, timeCreator).parseWithParseException(lexems);
    }
}
