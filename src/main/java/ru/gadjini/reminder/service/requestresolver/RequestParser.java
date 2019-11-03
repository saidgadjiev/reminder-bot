package ru.gadjini.reminder.service.requestresolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.service.requestresolver.postpone.lexer.PostponeLexem;
import ru.gadjini.reminder.service.requestresolver.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.requestresolver.reminder.RequestLexerParserFactory;
import ru.gadjini.reminder.service.requestresolver.reminder.lexer.ReminderLexem;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ParsedRequest;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ParsedTime;

import java.util.List;
import java.util.Locale;

@Service
public class RequestParser {

    private RequestLexerParserFactory parserFactory;

    @Autowired
    public RequestParser(RequestLexerParserFactory parserFactory) {
        this.parserFactory = parserFactory;
    }

    public ParsedRequest parseRequest(String text) {
        List<ReminderLexem> lexems = parserFactory.getLexerForLocale(Locale.getDefault(), text).tokenize();

        return parserFactory.getParserForLocale(Locale.getDefault()).parse(lexems);
    }

    public ParsedTime parseTime(String time) {
        List<ReminderLexem> lexems = parserFactory.getLexerForLocale(Locale.getDefault(), time).tokenizeTime();

        return parserFactory.getParserForLocale(Locale.getDefault()).parseTime(lexems);
    }

    public ParsedPostponeTime parsePostponeTime(String time) {
        List<PostponeLexem> lexems = parserFactory.getPostponeRequestLexer(Locale.getDefault(), time).tokenize();

        return parserFactory.getPostponeParserForLocale(Locale.getDefault()).parse(lexems);
    }
}
