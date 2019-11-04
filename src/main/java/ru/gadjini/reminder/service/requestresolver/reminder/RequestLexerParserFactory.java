package ru.gadjini.reminder.service.requestresolver.reminder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.requestresolver.postpone.lexer.PostponeRequestLexer;
import ru.gadjini.reminder.service.requestresolver.postpone.parser.ParsedPostponeTime;
import ru.gadjini.reminder.service.requestresolver.postpone.parser.PostponeRequestParser;
import ru.gadjini.reminder.service.requestresolver.reminder.lexer.ReminderRequestLexer;
import ru.gadjini.reminder.service.requestresolver.reminder.parser.ReminderRequestParser;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class RequestLexerParserFactory {

    private LocalisationService localisationService;

    private Map<Locale, Pattern> timePatterns = new HashMap<>();

    private Map<Locale, Map<ParsedPostponeTime.Type, Pattern>> postponePatterns = new HashMap<>();

    private Pattern loginStartPattern = Pattern.compile("^(@(?<login>[0-9a-zA-Z_]+) )?(?<text>[a-zA-Zа-яА-Я ]+)$");

    @Autowired
    public RequestLexerParserFactory(LocalisationService localisationService) {
        this.localisationService = localisationService;

        timePatterns.put(Locale.getDefault(), buildTimePatternForLocale(Locale.getDefault()));
        postponePatterns.put(Locale.getDefault(), buildPatternForPostponeTime(Locale.getDefault()));
    }

    public ReminderRequestLexer getLexerForLocale(Locale locale, String text) {
        return new ReminderRequestLexer(timePatterns.get(locale), loginStartPattern, text);
    }

    public ReminderRequestParser getParserForLocale(Locale locale) {
        return new ReminderRequestParser(localisationService, locale);
    }

    public PostponeRequestParser getPostponeParserForLocale(Locale locale) {
        return new PostponeRequestParser(localisationService, locale);
    }

    public PostponeRequestLexer getPostponeRequestLexer(Locale locale, String text) {
        return new PostponeRequestLexer(postponePatterns.get(locale), text);
    }

    private Pattern buildTimePatternForLocale(Locale locale) {
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.REGEXP_TIME_ARTICLE);

        StringBuilder patternBuilder = new StringBuilder();

        patternBuilder.append("((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9])) (").append(regexpTimeArticle).append(" )?((?<monthword>");

        for (Iterator<Month> iterator = Arrays.asList(Month.values()).iterator(); iterator.hasNext(); ) {
            Month month = iterator.next();

            patternBuilder.append(month.getDisplayName(TextStyle.FULL, locale));
            if (iterator.hasNext()) {
                patternBuilder.append("|");
            }
        }
        String tomorrow = localisationService.getMessage(MessagesProperties.REGEXP_TOMORROW);
        String dayAfterTomorrow = localisationService.getMessage(MessagesProperties.REGEXP_DAY_AFTER_TOMORROW);
        patternBuilder
                .append(") )?(((?<month>1[0-2]|[1-9])\\.)?(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>")
                .append(tomorrow).append("|").append(dayAfterTomorrow).append("))?");

        return Pattern.compile(patternBuilder.toString());
    }

    private Map<ParsedPostponeTime.Type, Pattern> buildPatternForPostponeTime(Locale locale) {
        StringBuilder patternTypeOn = new StringBuilder();

        String daySuffix = localisationService.getMessage(MessagesProperties.REGEX_POSTPONE_DAY);
        patternTypeOn.append("((?<onday>\\d+)").append(daySuffix).append(")?( )?");

        String hourSuffix = localisationService.getMessage(MessagesProperties.REGEX_POSTPONE_HOUR);
        patternTypeOn.append("((?<onhour>\\d+)").append(hourSuffix).append(")?( )?");

        String minuteSuffix = localisationService.getMessage(MessagesProperties.REGEX_POSTPONE_MINUTE);
        patternTypeOn.append("((?<onminute>\\d+)").append(minuteSuffix).append(")?");

        return Map.ofEntries(
                Map.entry(ParsedPostponeTime.Type.ON, Pattern.compile(patternTypeOn.toString())),
                Map.entry(ParsedPostponeTime.Type.AT, buildTimePatternForLocale(locale))
        );
    }
}
