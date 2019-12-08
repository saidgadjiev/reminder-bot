package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.regex.GroupPattern;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PatternBuilder {

    public static final String DAYS = "days";

    public static final String HOURS = "hours";

    public static final String MINUTES = "minutes";

    public static final String ONE_DAY = "oneday";

    public static final String DAY = "day";

    public static final String HOUR = "hour";

    public static final String MINUTE = "minute";

    public static final String DAY_WORD = "dayword";

    public static final String MONTH_WORD = "monthword";

    public static final String DAY_OF_WEEK_WORD = "dayofweek";

    public static final String NEXT_WEEK = "nextweek";

    public static final String MONTH = "month";

    public static final String LOGIN = "login";

    public static final String TEXT = "text";

    public static final String TYPE = "type";

    public static final String TTYPE = "ttype";

    public static final String THOUR = "thour";

    public static final String TMINUTE = "tminute";

    private LocalisationService localisationService;

    private DayOfWeekService dayOfWeekService;

    @Autowired
    public PatternBuilder(LocalisationService localisationService, DayOfWeekService dayOfWeekService) {
        this.localisationService = localisationService;
        this.dayOfWeekService = dayOfWeekService;
    }

    public GroupPattern buildLoginPattern() {
        return new GroupPattern(Pattern.compile("^(@(?<login>[0-9a-zA-Z_]+) )?(?<text>[a-zA-Zа-яА-ЯёЁ1-9 ]+)$"), List.of(LOGIN, TEXT));
    }

    public GroupPattern buildRepeatTimePattern(Locale locale) {
        StringBuilder pattern = new StringBuilder();

        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX);
        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX);
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.REGEXP_TIME_ARTICLE);
        String regexRepeat = localisationService.getMessage(MessagesProperties.REGEXP_REPEAT);
        String regexpDay = localisationService.getMessage(MessagesProperties.REGEXP_DAY);
        String regexpDays = localisationService.getMessage(MessagesProperties.REGEXP_DAYS);
        pattern.append("((((?<").append(MINUTES).append(">\\d+)").append(minutePrefix).append(")?( )?");
        pattern.append("((?<").append(HOURS).append(">\\d+)").append(hourPrefix).append(")?( )?)|");
        pattern.append("(((?<").append(HOUR).append(">2[0-3]|[01]?[0-9]):(?<").append(MINUTE).append(">[0-5]?[0-9]))( )?(").append(regexpTimeArticle).append(" )?((?<");
        pattern.append(DAY_OF_WEEK_WORD).append(">").append(getDayOfWeekPattern(locale));
        pattern.append(")|((?<").append(ONE_DAY).append(">").append(regexpDay).append(")|((?<").append(DAYS).append(">\\d+)").append(regexpDays).append(")").append(")))) ").append(regexRepeat);

        return new GroupPattern(Pattern.compile(pattern.toString()), List.of(MINUTES, HOURS, HOUR, MINUTE, DAY_OF_WEEK_WORD, ONE_DAY, DAYS));
    }

    public GroupPattern buildTimePattern(Locale locale) {
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.REGEXP_TIME_ARTICLE);
        String regexpDayOfWeekArticle = localisationService.getMessage(MessagesProperties.REGEXP_DAY_OF_WEEK_ARTICLE);
        String regexpNextWeek = localisationService.getMessage(MessagesProperties.REGEXP_NEXT_WEEK);
        StringBuilder patternBuilder = new StringBuilder();

        patternBuilder.append("((?<").append(HOUR).append(">2[0-3]|[01]?[0-9]):(?<").append(MINUTE).append(">[0-5]?[0-9]))( )?(")
                .append(regexpTimeArticle)
                .append(" )?((?<").append(DAY_OF_WEEK_WORD).append(">")
                .append(getDayOfWeekPattern(locale))
                .append(")").append("( (?<").append(NEXT_WEEK).append(">").append(regexpNextWeek).append("))?( (").append(regexpDayOfWeekArticle).append("))?( )?)?")
                .append("((?<").append(MONTH_WORD).append(">")
                .append(Stream.of(Month.values()).map(month -> month.getDisplayName(TextStyle.FULL, locale)).collect(Collectors.joining("|")));

        String tomorrow = localisationService.getMessage(MessagesProperties.REGEXP_TOMORROW);
        String dayAfterTomorrow = localisationService.getMessage(MessagesProperties.REGEXP_DAY_AFTER_TOMORROW);
        patternBuilder
                .append(") )?(((?<").append(MONTH).append(">1[0-2]|[1-9])\\.)?(?<").append(DAY).append(">0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<").append(DAY_WORD).append(">")
                .append(tomorrow).append("|").append(dayAfterTomorrow).append("))?");

        return new GroupPattern(Pattern.compile(patternBuilder.toString()), List.of(HOUR, MINUTE, DAY_OF_WEEK_WORD, NEXT_WEEK, MONTH_WORD, MONTH, DAY, DAY_WORD));
    }

    public GroupPattern buildPostponePattern() {
        StringBuilder patternTypeOn = new StringBuilder();

        String dayPrefix = localisationService.getMessage(MessagesProperties.REGEX_DAY_PREFIX);
        patternTypeOn.append("((?<").append(DAYS).append(">\\d+)").append(dayPrefix).append(")?( )?");

        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX);
        patternTypeOn.append("((?<").append(HOURS).append(">\\d+)").append(hourPrefix).append(")?( )?");

        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX);
        patternTypeOn.append("((?<").append(MINUTES).append(">\\d+)").append(minutePrefix).append(")?");

        return new GroupPattern(Pattern.compile(patternTypeOn.toString()), List.of(DAYS, HOURS, MINUTES));
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
                .append(")?( )?((?<").append(MINUTE).append(">\\d+)").append(minutePrefix).append(")?)|")
                .append("((?<").append(TTYPE).append(">").append(at).append(") (?<").append(THOUR).append(">2[0-3]|[01]?[0-9]):(?<").append(TMINUTE).append(">[0-5]?[0-9]))");

        return new GroupPattern(Pattern.compile(patternBuilder.toString()), List.of(TYPE, HOUR, MINUTE, TTYPE, THOUR, TMINUTE));
    }

    private String getDayOfWeekPattern(Locale locale) {
        StringBuilder pattern = new StringBuilder();

        pattern.append(fullDayOfWeekPattenForLocaleRu());
        pattern.append("|")
                .append(Arrays.stream(DayOfWeek.values()).map(dayOfWeek -> {
                    return dayOfWeek.getDisplayName(TextStyle.SHORT, locale);
                }).collect(Collectors.joining("|")));

        return pattern.toString();
    }

    private String fullDayOfWeekPattenForLocaleRu() {
        StringBuilder pattern = new StringBuilder();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            pattern.append(dayOfWeekService.getFullDisplayNamePattern(dayOfWeek)).append("|");
        }

        return pattern.substring(0, pattern.length() - 1);
    }

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("(((?<minutes>\\d+мин)?( )?(?<hours>\\d+ч)?( )?)|(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))( )?(в )?((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)|((?<oneday>день)|(?<days>\\d+дня))))) кажд[а-я]{0,2}");
        Pattern p = Pattern.compile(      "(((?<minutes>\\d+мин)?( )?(?<hours>\\d+ч)?( )?)|(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))( )?(в )?((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)|((?<oneday>день)|(?<days>\\d+дня))))) кажд[а-я]{0,2}");
        String str = "каждый понедельник в 19:00";
        String reverse = StringUtils.reverseDelimited(str, ' ');
        System.out.println(reverse);
        Matcher matcher = pattern.matcher(reverse);
        System.out.println(matcher.matches());

    }
}
