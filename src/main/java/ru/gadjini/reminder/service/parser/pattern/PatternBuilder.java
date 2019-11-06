package ru.gadjini.reminder.service.parser.pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.pattern.GroupPattern;
import ru.gadjini.reminder.service.LocalisationService;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class PatternBuilder {

    public static final String POSTPONE_DAY = "day";

    public static final String POSTPONE_HOUR = "hour";

    public static final String POSTPONE_MINUTE = "minute";

    public static final String DAY = "day";

    public static final String HOUR = "hour";

    public static final String MINUTE = "minute";

    public static final String DAY_WORD = "dayword";

    public static final String MONTH_WORD = "monthword";

    public static final String MONTH = "month";

    public static final String LOGIN = "login";

    public static final String TEXT = "text";

    public static final String TYPE = "type";

    public static final String TTYPE = "ttype";

    public static final String THOUR = "thour";

    public static final String TMINUTE = "tminute";

    private LocalisationService localisationService;

    @Autowired
    public PatternBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public GroupPattern buildLoginPattern() {
        return new GroupPattern(Pattern.compile("^(@(?<login>[0-9a-zA-Z_]+) )?(?<text>[a-zA-Zа-яА-Я ]+)$"), List.of(LOGIN, TEXT));
    }

    public GroupPattern buildTimePattern(Locale locale) {
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.REGEXP_TIME_ARTICLE);

        StringBuilder patternBuilder = new StringBuilder();

        patternBuilder.append("((?<").append(HOUR).append(">2[0-3]|[01]?[0-9]):(?<").append(MINUTE).append(">[0-5]?[0-9])) (")
                .append(regexpTimeArticle).append(" )?((?<").append(MONTH_WORD).append(">");

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
                .append(") )?(((?<").append(MONTH).append(">1[0-2]|[1-9])\\.)?(?<").append(DAY).append(">0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<").append(DAY_WORD).append(">")
                .append(tomorrow).append("|").append(dayAfterTomorrow).append("))?");

        return new GroupPattern(Pattern.compile(patternBuilder.toString()), List.of(HOUR, MINUTE, MONTH_WORD, MONTH, DAY, DAY_WORD));
    }

    public GroupPattern buildPostponePattern() {
        StringBuilder patternTypeOn = new StringBuilder();

        String dayPrefix = localisationService.getMessage(MessagesProperties.REGEX_DAY_PREFIX);
        patternTypeOn.append("((?<").append(POSTPONE_DAY).append(">\\d+)").append(dayPrefix).append(")?( )?");

        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX);
        patternTypeOn.append("((?<").append(POSTPONE_HOUR).append(">\\d+)").append(hourPrefix).append(")?( )?");

        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX);
        patternTypeOn.append("((?<").append(POSTPONE_MINUTE).append(">\\d+)").append(minutePrefix).append(")?");

        return new GroupPattern(Pattern.compile(patternTypeOn.toString()), List.of(POSTPONE_DAY, POSTPONE_HOUR, POSTPONE_MINUTE));
    }

    public GroupPattern buildCustomRemindPattern() {
        StringBuilder patternBuilder = new StringBuilder();

        String typeAfter = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_AFTER);
        String typeBefore = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_BEFORE);
        String at = localisationService.getMessage(MessagesProperties.REGEX_CUSTOM_REMIND_TYPE_AT);
        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX);
        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX);
        patternBuilder.append("((?<").append(TYPE).append(">").append(typeAfter).append("|")
                .append(typeBefore).append(") ((?<").append(HOUR).append(">\\d+)").append(hourPrefix)
                .append(") ( )?((?<").append(MINUTE).append(">\\d+)").append(minutePrefix).append(")?)|")
                .append("((?<").append(TTYPE).append(">").append(at).append(") (?<").append(THOUR).append(">2[0-3]|[01]?[0-9]):(?<").append(TMINUTE).append(">[0-5]?[0-9]))");

        return new GroupPattern(Pattern.compile(patternBuilder.toString()), List.of(TYPE, HOUR, MINUTE, TTYPE, THOUR, TMINUTE));
    }
}
