package ru.gadjini.reminder.service.parser.pattern;

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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PatternBuilder {

    public static final String SUFFIX_MINUTES = "suffixminutes";

    public static final String PREFIX_MINUTES = "prefixminutes";

    public static final String SUFFIX_HOURS = "suffixhours";

    public static final String PREFIX_HOURS = "prefixhours";

    public static final String SUFFIX_DAYS = "suffixdays";

    public static final String PREFIX_DAYS = "prefixdays";

    public static final String SUFFIX_MONTHS = "suffixmonths";

    public static final String PREFIX_MONTHS = "prefixmonths";

    public static final String PREFIX_YEARS = "prefixyears";

    public static final String SUFFIX_YEARS = "suffixyears";

    public static final String ONE_DAY = "oneday";

    public static final String ONE_MINUTE = "oneminute";

    public static final String ONE_HOUR = "onehour";

    public static final String ONE_MONTH = "onemonth";

    public static final String ONE_YEAR = "oneyear";

    public static final String DAY = "day";

    public static final String HOUR = "hour";

    public static final String MINUTE = "minute";

    public static final String DAY_WORD = "dayword";

    public static final String MONTH_WORD = "monthword";

    public static final String DAY_OF_WEEK_WORD = "dayofweek";

    public static final String NEXT_WEEK = "nextweek";

    public static final String MONTH = "month";

    public static final String YEAR = "year";

    public static final String TEXT = "text";

    public static final String TYPE = "type";

    public static final String EVERY_MONTH_DAY = "everymonthday";

    private LocalisationService localisationService;

    private DayOfWeekService dayOfWeekService;

    @Autowired
    public PatternBuilder(LocalisationService localisationService, DayOfWeekService dayOfWeekService) {
        this.localisationService = localisationService;
        this.dayOfWeekService = dayOfWeekService;
    }

    public GroupPattern buildRepeatTimePattern(Locale locale) {
        StringBuilder pattern = new StringBuilder();

        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX);
        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX);
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        String regexRepeat = localisationService.getMessage(MessagesProperties.REGEXP_REPEAT);
        String regexpEveryDay = localisationService.getMessage(MessagesProperties.REGEXP_DAY);
        String regexpEveryMinute = localisationService.getMessage(MessagesProperties.REGEXP_MINUTE);
        String regexpEveryMonthDayPrefix = localisationService.getMessage(MessagesProperties.REGEXP_MONTH_DAY_PREFIX);
        String regexpEveryHour = localisationService.getMessage(MessagesProperties.REGEXP_HOUR);
        String regexpEveryMonth = localisationService.getMessage(MessagesProperties.REGEXP_MONTH);
        String regexpEveryYear = localisationService.getMessage(MessagesProperties.REGEXP_YEAR);
        String dayPrefix = localisationService.getMessage(MessagesProperties.REGEX_DAY_PREFIX);
        String monthPrefix = localisationService.getMessage(MessagesProperties.REGEXP_MONTH_PREFIX);

        pattern.append("((\\b(?<").append(HOUR).append(">2[0-3]|[01]?[0-9])(:(?<").append(MINUTE).append(">[0-5]?[0-9]))?\\b ?)(")
                .append(regexpTimeArticle).append(" ?)?)?(((((").append(minutePrefix).append(") )(?<").append(PREFIX_MINUTES).append(">\\d+)|(?<")
                .append(SUFFIX_MINUTES).append(">\\d+)(").append(minutePrefix).append(")( )?)?(( )?(((").append(hourPrefix).append(") )(?<")
                .append(PREFIX_HOURS).append(">\\d+)|(?<").append(SUFFIX_HOURS).append(">\\d+)(").append(hourPrefix).append(")( )?))?(( )?(((")
                .append(dayPrefix).append(") )(?<").append(PREFIX_DAYS).append(">\\d+)|(?<").append(SUFFIX_DAYS).append(">\\d+)(")
                .append(dayPrefix).append(")))?)|(?<").append(ONE_HOUR).append(">").append(regexpEveryHour)
                .append(")|(?<").append(ONE_DAY).append(">").append(regexpEveryDay).append(")|(((").append(regexpEveryMonthDayPrefix)
                .append(" )?(?<").append(EVERY_MONTH_DAY).append(">\\d+)(").append(regexpEveryMonthDayPrefix).append(")?) ((?<")
                .append(ONE_MONTH).append(">").append(regexpEveryMonth).append(")|(((").append(monthPrefix).append(") )(?<")
                .append(PREFIX_MONTHS).append(">\\d+)|(?<").append(SUFFIX_MONTHS).append(">\\d+)(").append(monthPrefix).append("))))|((?<").append(MONTH_WORD).append(">")
                .append(Stream.of(Month.values()).map(month -> month.getDisplayName(TextStyle.FULL, locale)).collect(Collectors.joining("|")))
                .append(") (?<").append(DAY).append(">\\d+)( (?<").append(ONE_YEAR).append(">").append(regexpEveryYear).append("))?)|(?<")
                .append(ONE_MINUTE).append(">").append(regexpEveryMinute).append(")|(?<").append(DAY_OF_WEEK_WORD)
                .append(">").append(getDayOfWeekPattern(locale)).append(")) ").append(regexRepeat);

        return new GroupPattern(
                Pattern.compile(pattern.toString()),
                List.of(PREFIX_HOURS, SUFFIX_HOURS, ONE_HOUR, PREFIX_MINUTES, SUFFIX_MINUTES, ONE_MINUTE, ONE_YEAR, DAY, MONTH_WORD, SUFFIX_MONTHS, PREFIX_MONTHS, ONE_MONTH, EVERY_MONTH_DAY, ONE_DAY, PREFIX_DAYS, SUFFIX_DAYS, DAY_OF_WEEK_WORD, HOUR, MINUTE)
        );
    }

    public GroupPattern buildTimePattern(Locale locale) {
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        String regexpDayOfWeekArticle = localisationService.getMessage(MessagesProperties.REGEXP_DAY_OF_WEEK_ARTICLE);
        String regexpNextWeek = localisationService.getMessage(MessagesProperties.REGEXP_NEXT_WEEK);
        String tomorrow = localisationService.getMessage(MessagesProperties.TOMORROW);
        String dayAfterTomorrow = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW);
        String today = localisationService.getMessage(MessagesProperties.TODAY);
        String until = localisationService.getMessage(MessagesProperties.FIXED_TIME_TYPE_UNTIL);
        StringBuilder patternBuilder = new StringBuilder();

        patternBuilder.append("((\\b(?<").append(HOUR).append(">2[0-3]|[01]?[0-9])(:(?<").append(MINUTE).append(">[0-5]?[0-9]))?\\b ?)(")
                .append(regexpTimeArticle).append(" ?)?)?(((((?<").append(DAY_OF_WEEK_WORD).append(">")
                .append(getDayOfWeekPattern(locale)).append(") ?)((?<").append(NEXT_WEEK).append(">")
                .append(regexpNextWeek).append(") ?)?)((").append(regexpDayOfWeekArticle).append(") ?)?)|((((?<")
                .append(MONTH_WORD).append(">").append(Stream.of(Month.values()).map(month -> month.getDisplayName(TextStyle.FULL, locale)).collect(Collectors.joining("|")))
                .append(") )|(((?<").append(YEAR).append(">\\d{4})\\.)?(?<").append(MONTH).append(">1[0-2]|[1-9])\\.))((?<")
                .append(DAY).append(">0[1-9]|[12]\\d|3[01]|0?[1-9]) ?))|(?<").append(DAY_WORD).append(">\\b(").append(today).append("|")
                .append(tomorrow).append("|").append(dayAfterTomorrow).append(")\\b))?((?<").append(TYPE).append(">\\b")
                .append(until).append("\\b) ?)?");


        return new GroupPattern(Pattern.compile(patternBuilder.toString()), List.of(TYPE, YEAR, MONTH, DAY, DAY_WORD, MONTH_WORD, NEXT_WEEK, DAY_OF_WEEK_WORD, HOUR, MINUTE));
    }

    public GroupPattern buildOffsetTimePattern() {
        StringBuilder patternBuilder = new StringBuilder();

        String dayPrefix = localisationService.getMessage(MessagesProperties.REGEX_DAY_PREFIX);
        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX);
        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX);
        String eve = localisationService.getMessage(MessagesProperties.EVE);
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        String typeAfter = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_AFTER);
        String typeOn = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_FOR);
        String typeBefore = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_BEFORE);
        String hour = localisationService.getMessage(MessagesProperties.REGEXP_HOUR);
        String day = localisationService.getMessage(MessagesProperties.REGEXP_DAY);
        String yearPrefix = localisationService.getMessage(MessagesProperties.REGEXP_YEAR_PREFIX);
        String year = localisationService.getMessage(MessagesProperties.REGEXP_YEAR);
        String month = localisationService.getMessage(MessagesProperties.REGEXP_MONTH);
        String monthPrefix = localisationService.getMessage(MessagesProperties.REGEXP_MONTH_PREFIX);

        patternBuilder.append("((\\b(?<").append(HOUR).append(">2[0-3]|[01]?[0-9])(:(?<").append(MINUTE).append(">[0-5]?[0-9]))?\\b ?)(")
                .append(timeArticle).append(" ?)?)?((((").append(minutePrefix).append(") )(?<").append(PREFIX_MINUTES).append(">\\d+)|(?<")
                .append(SUFFIX_MINUTES).append(">\\d+)(").append(minutePrefix).append(")( )?)?(( )?((?<").append(ONE_HOUR)
                .append(">").append(hour).append(")|((").append(hourPrefix).append(") )(?<").append(PREFIX_HOURS).append(">\\d+)|(?<")
                .append(SUFFIX_HOURS).append(">\\d+)(").append(hourPrefix).append(")( )?))?(( )?((?<").append(ONE_DAY).append(">")
                .append(day).append(")|((").append(dayPrefix).append(") )(?<").append(PREFIX_DAYS).append(">\\d+)|(?<").append(SUFFIX_DAYS)
                .append(">\\d+)(").append(dayPrefix).append(")))?(( )?((?<" + ONE_MONTH + ">").append(month).append(")|((")
                .append(monthPrefix).append(") )(?<").append(PREFIX_MONTHS).append(">\\d+)|(?<").append(SUFFIX_MONTHS).append(">\\d+)(")
                .append(monthPrefix).append(")))?(( )?((?<").append(ONE_YEAR).append(">").append(year).append(")|((").append(yearPrefix)
                .append(") )(?<").append(PREFIX_YEARS).append(">\\d+)|(?<").append(SUFFIX_YEARS).append(">\\d+)(").append(yearPrefix)
                .append(")))?) (?<").append(TYPE).append(">\\b(").append(eve).append("|").append(typeAfter).append("|")
                .append(typeBefore).append("|").append(typeOn).append(")\\b)");

        return new GroupPattern(Pattern.compile(patternBuilder.toString()), List.of(TYPE, SUFFIX_YEARS, PREFIX_YEARS, ONE_YEAR,
                SUFFIX_MONTHS, PREFIX_MONTHS, ONE_MONTH, SUFFIX_DAYS, PREFIX_DAYS, ONE_DAY, SUFFIX_HOURS,
                PREFIX_HOURS, ONE_HOUR, SUFFIX_MINUTES, PREFIX_MINUTES, HOUR, MINUTE));
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
}
