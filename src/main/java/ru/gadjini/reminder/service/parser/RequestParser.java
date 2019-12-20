package ru.gadjini.reminder.service.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.postpone.lexer.PostponeLexerConfig;
import ru.gadjini.reminder.service.parser.postpone.lexer.PostponeRequestLexer;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeRequestParser;
import ru.gadjini.reminder.service.parser.remind.lexer.CustomRemindLexer;
import ru.gadjini.reminder.service.parser.remind.parser.CustomRemindParser;
import ru.gadjini.reminder.service.parser.remind.parser.CustomRemindTime;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderRequestLexer;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderRequestLexerConfig;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedRequest;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequestParser;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexerConfig;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class RequestParser {

    private LocalisationService localisationService;

    private final PostponeLexerConfig postponeLexerConfig;

    private final ReminderRequestLexerConfig reminderRequestLexerConfig;

    private final TimeLexerConfig timeLexerConfig;

    private DayOfWeekService dayOfWeekService;

    @Autowired
    public RequestParser(LocalisationService localisationService,
                         PostponeLexerConfig postponeLexerConfig,
                         ReminderRequestLexerConfig reminderRequestLexerConfig,
                         TimeLexerConfig timeLexerConfig,
                         DayOfWeekService dayOfWeekService) {
        this.localisationService = localisationService;
        this.postponeLexerConfig = postponeLexerConfig;
        this.reminderRequestLexerConfig = reminderRequestLexerConfig;
        this.timeLexerConfig = timeLexerConfig;
        this.dayOfWeekService = dayOfWeekService;
    }

    public ParsedRequest parseRequest(String text, ZoneId zoneId) {
        List<BaseLexem> lexems = new ReminderRequestLexer(reminderRequestLexerConfig, timeLexerConfig, text).tokenize();

        return new ReminderRequestParser(localisationService, Locale.getDefault(), zoneId, dayOfWeekService).parse(lexems);
    }

    public ZonedDateTime parseTime(String time, ZoneId zoneId) {
        List<BaseLexem> lexems = new ReminderRequestLexer(reminderRequestLexerConfig, timeLexerConfig, time).tokenizeTime();

        return new ReminderRequestParser(localisationService, Locale.getDefault(), zoneId, dayOfWeekService).parseTime(lexems).toZonedDateTime();
    }

    public ParsedPostponeTime parsePostponeTime(String time, ZoneId zoneId) {
        List<BaseLexem> lexems = new PostponeRequestLexer(postponeLexerConfig, timeLexerConfig, time).tokenize();

        return new PostponeRequestParser(localisationService, Locale.getDefault(), zoneId, dayOfWeekService).parse(lexems);
    }

    public CustomRemindTime parseCustomRemind(String text, ZoneId zoneId) {
        List<BaseLexem> lexems = new CustomRemindLexer(timeLexerConfig, text).tokenize();

        return new CustomRemindParser(localisationService, Locale.getDefault(), zoneId, dayOfWeekService).parse(lexems);
    }
}
