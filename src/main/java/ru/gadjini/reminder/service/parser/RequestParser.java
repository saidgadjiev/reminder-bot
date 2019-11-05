package ru.gadjini.reminder.service.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.parser.postpone.lexer.PostponeLexem;
import ru.gadjini.reminder.service.parser.postpone.lexer.PostponeLexerConfig;
import ru.gadjini.reminder.service.parser.postpone.lexer.PostponeRequestLexer;
import ru.gadjini.reminder.service.parser.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.parser.postpone.parser.PostponeRequestParser;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderLexem;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderRequestLexer;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderRequestLexerConfig;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedRequest;
import ru.gadjini.reminder.service.parser.reminder.parser.ParsedTime;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequestParser;

import java.util.List;
import java.util.Locale;

@Service
public class RequestParser {

    private LocalisationService localisationService;

    private final PostponeLexerConfig postponeLexerConfig;

    private final ReminderRequestLexerConfig reminderRequestLexerConfig;

    @Autowired
    public RequestParser(LocalisationService localisationService, PostponeLexerConfig postponeLexerConfig, ReminderRequestLexerConfig reminderRequestLexerConfig) {
        this.localisationService = localisationService;
        this.postponeLexerConfig = postponeLexerConfig;
        this.reminderRequestLexerConfig = reminderRequestLexerConfig;
    }

    public ParsedRequest parseRequest(String text) {
        List<ReminderLexem> lexems = new ReminderRequestLexer(reminderRequestLexerConfig, text).tokenize();

        return new ReminderRequestParser(localisationService, Locale.getDefault()).parse(lexems);
    }

    public ParsedTime parseTime(String time) {
        List<ReminderLexem> lexems = new ReminderRequestLexer(reminderRequestLexerConfig, time).tokenizeTime();

        return new ReminderRequestParser(localisationService, Locale.getDefault()).parseTime(lexems);
    }

    public ParsedPostponeTime parsePostponeTime(String time) {
        List<PostponeLexem> lexems = new PostponeRequestLexer(postponeLexerConfig, time).tokenize();

        return new PostponeRequestParser(localisationService, Locale.getDefault()).parse(lexems);
    }
}
