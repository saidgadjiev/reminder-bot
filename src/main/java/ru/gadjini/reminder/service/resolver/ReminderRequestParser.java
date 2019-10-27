package ru.gadjini.reminder.service.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.resolver.lexer.Lexem;
import ru.gadjini.reminder.service.resolver.lexer.RequestLexer;
import ru.gadjini.reminder.service.resolver.parser.ParsedRequest;
import ru.gadjini.reminder.service.resolver.parser.ParsedTime;
import ru.gadjini.reminder.service.resolver.parser.RequestParser;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ReminderRequestParser {

    private final LocalisationService localisationService;

    private String tomorrow;

    private String dayAfterTomorrow;

    private Pattern loginStartPattern = Pattern.compile("^(@(?<login>[0-9a-zA-Z_]+) )?(?<text>[a-zA-Zа-яА-Я ]+)$");

    private Map<String, Pattern> timeStartPatterns = new HashMap<>();

    @Autowired
    public ReminderRequestParser(LocalisationService localisationService) {
        this.localisationService = localisationService;
        this.tomorrow = localisationService.getMessage(MessagesProperties.REGEXP_TOMORROW);
        this.dayAfterTomorrow = localisationService.getMessage(MessagesProperties.REGEXP_DAY_AFTER_TOMORROW);

        timeStartPatterns.put(Locale.getDefault().toString(), buildPatternForLocale());
    }

    public ParsedRequest parseRequest(String text) {
        Pattern timeStampPattern = timeStartPatterns.get(Locale.getDefault().toString());
        List<Lexem> lexems = new RequestLexer(timeStampPattern, loginStartPattern, text).tokenize();

        return new RequestParser(tomorrow, dayAfterTomorrow).parse(lexems);
    }

    public ParsedTime parseTime(String time) {
        Pattern timeStampPattern = timeStartPatterns.get(Locale.getDefault().toString());
        List<Lexem> lexems = new RequestLexer(timeStampPattern, loginStartPattern, time).tokenizeTime();

        return new RequestParser(tomorrow, dayAfterTomorrow).parseTime(lexems);
    }

    private Pattern buildPatternForLocale() {
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.REGEXP_TIME_ARTICLE);

        StringBuilder patternBuilder = new StringBuilder();

        patternBuilder.append("^((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9])) (").append(regexpTimeArticle).append(" )?((?<day>0?[1-9]|[12]\\d|3[01])|(?<dayword>");

        for (Iterator<String> iterator = Arrays.asList(tomorrow, dayAfterTomorrow).iterator(); iterator.hasNext(); ) {
            String val = iterator.next();

            patternBuilder.append(val);
            if (iterator.hasNext()) {
                patternBuilder.append("|");
            }
        }

        patternBuilder.append(") )?");

        return Pattern.compile(patternBuilder.toString());
    }
}
