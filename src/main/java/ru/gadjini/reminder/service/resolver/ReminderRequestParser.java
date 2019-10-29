package ru.gadjini.reminder.service.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.service.resolver.lexer.Lexem;
import ru.gadjini.reminder.service.resolver.parser.ParsedRequest;
import ru.gadjini.reminder.service.resolver.parser.ParsedTime;

import java.util.List;
import java.util.Locale;

@Service
public class ReminderRequestParser {

    private RequestLexerParserFactory parserFactory;

    @Autowired
    public ReminderRequestParser(RequestLexerParserFactory parserFactory) {
        this.parserFactory = parserFactory;
    }

    public ParsedRequest parseRequest(String text) {
        List<Lexem> lexems = parserFactory.getLexerForLocale(Locale.getDefault(), text).tokenize();

        return parserFactory.getParserForLocale(Locale.getDefault(), text).parse(lexems);
    }

    public ParsedTime parseTime(String time) {
        List<Lexem> lexems = parserFactory.getLexerForLocale(Locale.getDefault(), time).tokenizeTime();

        return parserFactory.getParserForLocale(Locale.getDefault(), time).parseTime(lexems);
    }
}
