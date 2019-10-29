package ru.gadjini.reminder.service.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.resolver.lexer.RequestLexer;
import ru.gadjini.reminder.service.resolver.parser.RequestParser;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class RequestLexerParserFactory {

    private LocalisationService localisationService;

    private Map<Locale, Pattern> timePatterns = new HashMap<>();

    private Pattern loginStartPattern = Pattern.compile("^(@(?<login>[0-9a-zA-Z_]+) )?(?<text>[a-zA-Zа-яА-Я ]+)$");

    @Autowired
    public RequestLexerParserFactory(LocalisationService localisationService) {
        this.localisationService = localisationService;

        timePatterns.put(Locale.getDefault(), buildPatternForLocale(Locale.getDefault()));
    }

    public RequestLexer getLexerForLocale(Locale locale, String text) {
        return new RequestLexer(timePatterns.get(locale), loginStartPattern, text);
    }

    public RequestParser getParserForLocale(Locale locale, String text) {
        return new RequestParser(localisationService, locale);
    }

    private Pattern buildPatternForLocale(Locale locale) {
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.REGEXP_TIME_ARTICLE);

        StringBuilder patternBuilder = new StringBuilder();

        patternBuilder.append("^((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9])) (").append(regexpTimeArticle).append(" )?((?<monthword>");

        for (Iterator<Month> iterator = Arrays.asList(Month.values()).iterator(); iterator.hasNext(); ) {
            Month month = iterator.next();

            patternBuilder.append(month.getDisplayName(TextStyle.FULL, locale));
            if (iterator.hasNext()) {
                patternBuilder.append("|");
            }
        }
        patternBuilder.append(") )?(((?<month>1[0-2]|[1-9])\\.)?(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>");

        String tomorrow = localisationService.getMessage(MessagesProperties.REGEXP_TOMORROW);
        String dayAfterTomorrow = localisationService.getMessage(MessagesProperties.REGEXP_DAY_AFTER_TOMORROW);

        for (Iterator<String> iterator = Arrays.asList(tomorrow, dayAfterTomorrow).iterator(); iterator.hasNext(); ) {
            String val = iterator.next();

            patternBuilder.append(val);
            if (iterator.hasNext()) {
                patternBuilder.append("|");
            }
        }
        patternBuilder.append("))?");

        return Pattern.compile(patternBuilder.toString());
    }
}
