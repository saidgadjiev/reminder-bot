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
import java.util.Collections;
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

    public static final String SUFFIX_WEEKS = "suffixweeks";

    public static final String PREFIX_WEEKS = "prefixweeks";

    public static final String ONE_WEEK = "oneweek";

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

    public static final String WEEKS_DAY_OF_WEEK_WORD = "weeksdayofweek";

    public static final String NEXT_WEEK = "nextweek";

    public static final String MONTH = "month";

    public static final String YEAR = "year";

    public static final String TEXT = "text";

    public static final String TYPE = "type";

    public static final String PREFIX_DAY_OF_MONTH = "prefixdayofmonth";

    public static final String SUFFIX_DAY_OF_MONTH = "suffixdayofmonth";

    public static final List<String> FIXED_TIME_PATTERN_GROUPS = List.of(TYPE, YEAR, MONTH, DAY, DAY_WORD, MONTH_WORD, NEXT_WEEK, DAY_OF_WEEK_WORD, HOUR, MINUTE);

    public static final List<String> REPEAT_TIME_PATTERN_GROUPS = List.of(
            PREFIX_HOURS, SUFFIX_HOURS, ONE_HOUR,
            PREFIX_MINUTES, SUFFIX_MINUTES, ONE_MINUTE,
            ONE_YEAR, DAY, MONTH_WORD, SUFFIX_MONTHS,
            PREFIX_MONTHS, ONE_MONTH, PREFIX_DAY_OF_MONTH, SUFFIX_DAY_OF_MONTH,
            WEEKS_DAY_OF_WEEK_WORD, ONE_WEEK, PREFIX_WEEKS, SUFFIX_WEEKS,
            ONE_DAY, PREFIX_DAYS, SUFFIX_DAYS,
            DAY_OF_WEEK_WORD, HOUR, MINUTE
    );

    public static final List<String> OFFSET_TIME_PATTERN_GROUPS = List.of(
            TYPE, SUFFIX_YEARS, PREFIX_YEARS,
            ONE_YEAR, SUFFIX_MONTHS, PREFIX_MONTHS,
            ONE_MONTH, WEEKS_DAY_OF_WEEK_WORD, ONE_WEEK, SUFFIX_WEEKS, PREFIX_WEEKS,
            SUFFIX_DAYS, PREFIX_DAYS,
            ONE_DAY, SUFFIX_HOURS, PREFIX_HOURS,
            ONE_HOUR, SUFFIX_MINUTES, PREFIX_MINUTES, ONE_MINUTE,
            HOUR, MINUTE
    );

    private LocalisationService localisationService;

    private DayOfWeekService dayOfWeekService;

    @Autowired
    public PatternBuilder(LocalisationService localisationService, DayOfWeekService dayOfWeekService) {
        this.localisationService = localisationService;
        this.dayOfWeekService = dayOfWeekService;
    }

    public GroupPattern buildRepeatWordPattern(Locale locale) {
        String regexRepeat = localisationService.getMessage(MessagesProperties.REGEXP_REPEAT, locale);

        return new GroupPattern(Pattern.compile(regexRepeat + "$"), Collections.emptyList());
    }

    public GroupPattern buildRepeatTimePattern(Locale locale) {
        StringBuilder pattern = new StringBuilder();

        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX, locale);
        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX, locale);
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
        String regexpEveryDay = localisationService.getMessage(MessagesProperties.REGEXP_DAY, locale);
        String regexpEveryMinute = localisationService.getMessage(MessagesProperties.REGEXP_MINUTE, locale);
        String regexpEveryMonthDayPrefix = localisationService.getMessage(MessagesProperties.REGEXP_MONTH_DAY_PREFIX, locale);
        String regexpEveryHour = localisationService.getMessage(MessagesProperties.REGEXP_HOUR, locale);
        String regexpEveryMonth = localisationService.getMessage(MessagesProperties.REGEXP_MONTH, locale);
        String regexpEveryYear = localisationService.getMessage(MessagesProperties.REGEXP_YEAR, locale);
        String dayPrefix = localisationService.getMessage(MessagesProperties.REGEX_DAY_PREFIX, locale);
        String monthPrefix = localisationService.getMessage(MessagesProperties.REGEXP_MONTH_PREFIX, locale);
        String weekRegexp = localisationService.getMessage(MessagesProperties.REGEX_WEEK_PREFIX, locale);
        String oneWeekRegexp = localisationService.getMessage(MessagesProperties.REGEXP_ONE_WEEK_PREFIX, locale);
        String regexpDayOfWeekArticle = localisationService.getMessage(MessagesProperties.REGEXP_DAY_OF_WEEK_ARTICLE, locale);

        pattern.append("((\\b(?<").append(HOUR).append(">2[0-3]|[01]?[0-9])(:(?<").append(MINUTE).append(">[0-5]?[0-9]))?\\b ?)(")
                .append(regexpTimeArticle).append(" ?)?)?((?<" + ONE_MINUTE + ">").append(regexpEveryMinute).append(")|((((")
                .append(minutePrefix).append(") )(?<").append(PREFIX_MINUTES).append(">\\d+)|(?<")
                .append(SUFFIX_MINUTES).append(">\\d+)(").append(minutePrefix).append(")( )?)?(( )?((?<" + ONE_HOUR + ">")
                .append(regexpEveryHour).append(")|((").append(hourPrefix).append(") )(?<")
                .append(PREFIX_HOURS).append(">\\d+)|(?<").append(SUFFIX_HOURS).append(">\\d+)(").append(hourPrefix)
                .append(")( )?))?(( )?((?<" + ONE_DAY + ">").append(regexpEveryDay).append(")|((")
                .append(dayPrefix).append(") )(?<").append(PREFIX_DAYS).append(">\\d+)|(?<").append(SUFFIX_DAYS).append(">\\d+)(")
                .append(dayPrefix).append(")))?(( )?((((?<" + WEEKS_DAY_OF_WEEK_WORD + ">").append(getDayOfWeekPattern(locale))
                .append(") )((").append(regexpDayOfWeekArticle).append(") ?)?)?((?<").append(ONE_WEEK).append(">")
                .append(oneWeekRegexp).append(")|((")
                .append(weekRegexp).append(") )(?<").append(PREFIX_WEEKS).append(">\\d+)|(?<").append(SUFFIX_WEEKS)
                .append(">\\d+)(").append(weekRegexp).append("))))?(((").append(regexpEveryMonthDayPrefix).append(" )(?<")
                .append(PREFIX_DAY_OF_MONTH).append(">\\d+)|(?<").append(SUFFIX_DAY_OF_MONTH).append(">\\d+)(")
                .append(regexpEveryMonthDayPrefix).append("))?(( )?((?<").append(ONE_MONTH).append(">").append(regexpEveryMonth)
                .append(")|(((").append(monthPrefix).append(") )(?<").append(PREFIX_MONTHS).append(">\\d+)|(?<")
                .append(SUFFIX_MONTHS).append(">\\d+)(").append(monthPrefix).append(")))))?)|((?<").append(MONTH_WORD).append(">")
                .append(Stream.of(Month.values()).map(month -> month.getDisplayName(TextStyle.FULL, locale)).collect(Collectors.joining("|")))
                .append(") (?<").append(DAY).append(">\\d+)( (?<").append(ONE_YEAR).append(">").append(regexpEveryYear).append("))?)|(?<").append(DAY_OF_WEEK_WORD)
                .append(">").append(getDayOfWeekPattern(locale)).append("))");

        return new GroupPattern(Pattern.compile(pattern.toString()), REPEAT_TIME_PATTERN_GROUPS);
    }

    public GroupPattern buildTimePattern(Locale locale) {
        String regexpTimeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
        String regexpDayOfWeekArticle = localisationService.getMessage(MessagesProperties.REGEXP_DAY_OF_WEEK_ARTICLE, locale);
        String regexpNextWeek = localisationService.getMessage(MessagesProperties.REGEXP_NEXT_WEEK, locale);
        String tomorrow = localisationService.getMessage(MessagesProperties.TOMORROW, locale);
        String dayAfterTomorrow = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW, locale);
        String today = localisationService.getMessage(MessagesProperties.TODAY, locale);
        String until = localisationService.getMessage(MessagesProperties.FIXED_TIME_TYPE_UNTIL, locale);
        StringBuilder patternBuilder = new StringBuilder();

        patternBuilder.append("((\\b(?<").append(HOUR).append(">2[0-3]|[01]?[0-9])(:(?<").append(MINUTE).append(">[0-5]?[0-9]))?\\b ?)(")
                .append(regexpTimeArticle).append(" ?)?)?(((((?<").append(DAY_OF_WEEK_WORD).append(">")
                .append(getDayOfWeekPattern(locale)).append(") ?)((?<").append(NEXT_WEEK).append(">")
                .append(regexpNextWeek).append(") ?)?)((").append(regexpDayOfWeekArticle).append(") ?)?)|((((?<")
                .append(MONTH_WORD).append(">").append(Stream.of(Month.values()).map(month -> month.getDisplayName(TextStyle.FULL, locale)).collect(Collectors.joining("|")))
                .append(") )|(((?<").append(YEAR).append(">\\d{4})\\.)?(?<").append(MONTH).append(">1[0-2]|0?[1-9])\\.))((?<")
                .append(DAY).append(">0[1-9]|[12]\\d|3[01]|0?[1-9]) ?))|(?<").append(DAY_WORD).append(">\\b(").append(today).append("|")
                .append(tomorrow).append("|").append(dayAfterTomorrow).append(")\\b))?((?<").append(TYPE).append(">\\b")
                .append(until).append("\\b) ?)?");


        return new GroupPattern(Pattern.compile(patternBuilder.toString()), FIXED_TIME_PATTERN_GROUPS);
    }

    public GroupPattern buildOffsetTimePattern(Locale locale) {
        StringBuilder patternBuilder = new StringBuilder();

        String dayPrefix = localisationService.getMessage(MessagesProperties.REGEX_DAY_PREFIX, locale);
        String hourPrefix = localisationService.getMessage(MessagesProperties.REGEX_HOUR_PREFIX, locale);
        String minutePrefix = localisationService.getMessage(MessagesProperties.REGEX_MINUTE_PREFIX, locale);
        String eve = localisationService.getMessage(MessagesProperties.EVE, locale);
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
        String typeAfter = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_AFTER, locale);
        String typeOn = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_FOR, locale);
        String typeBefore = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_BEFORE, locale);
        String hour = localisationService.getMessage(MessagesProperties.REGEXP_HOUR, locale);
        String day = localisationService.getMessage(MessagesProperties.REGEXP_DAY, locale);
        String yearPrefix = localisationService.getMessage(MessagesProperties.REGEXP_YEAR_PREFIX, locale);
        String year = localisationService.getMessage(MessagesProperties.REGEXP_YEAR, locale);
        String month = localisationService.getMessage(MessagesProperties.REGEXP_MONTH, locale);
        String monthPrefix = localisationService.getMessage(MessagesProperties.REGEXP_MONTH_PREFIX, locale);
        String minute = localisationService.getMessage(MessagesProperties.REGEXP_MINUTE, locale);
        String weekRegexp = localisationService.getMessage(MessagesProperties.REGEX_WEEK_PREFIX, locale);
        String oneWeekRegexp = localisationService.getMessage(MessagesProperties.REGEXP_ONE_WEEK_PREFIX, locale);
        String regexpDayOfWeekArticle = localisationService.getMessage(MessagesProperties.REGEXP_DAY_OF_WEEK_ARTICLE, locale);

        patternBuilder.append("((\\b(?<").append(HOUR).append(">2[0-3]|[01]?[0-9])(:(?<").append(MINUTE).append(">[0-5]?[0-9]))?\\b ?)(")
                .append(timeArticle).append(" ?)?)?((?<").append(ONE_MINUTE).append(">").append(minute).append(")|(((")
                .append(minutePrefix).append(") )(?<").append(PREFIX_MINUTES).append(">\\d+)|(?<")
                .append(SUFFIX_MINUTES).append(">\\d+)(").append(minutePrefix).append(")( )?)?(( )?((?<").append(ONE_HOUR)
                .append(">").append(hour).append(")|((").append(hourPrefix).append(") )(?<").append(PREFIX_HOURS).append(">\\d+)|(?<")
                .append(SUFFIX_HOURS).append(">\\d+)(").append(hourPrefix).append(")( )?))?(( )?((?<").append(ONE_DAY).append(">")
                .append(day).append(")|((").append(dayPrefix).append(") )(?<").append(PREFIX_DAYS).append(">\\d+)|(?<").append(SUFFIX_DAYS)
                .append(">\\d+)(").append(dayPrefix).append(")))?(( )?((((?<" + WEEKS_DAY_OF_WEEK_WORD + ">")
                .append(getDayOfWeekPattern(locale)).append(") )((").append(regexpDayOfWeekArticle).append(") ?)?)?((?<")
                .append(ONE_WEEK).append(">").append(oneWeekRegexp).append(")|((")
                .append(weekRegexp).append(") )(?<").append(PREFIX_WEEKS).append(">\\d+)|(?<").append(SUFFIX_WEEKS).append(">\\d+)(")
                .append(weekRegexp).append("))))?(( )?((?<").append(ONE_MONTH).append(">").append(month).append(")|((")
                .append(monthPrefix).append(") )(?<").append(PREFIX_MONTHS).append(">\\d+)|(?<").append(SUFFIX_MONTHS).append(">\\d+)(")
                .append(monthPrefix).append(")))?(( )?((?<").append(ONE_YEAR).append(">").append(year).append(")|((").append(yearPrefix)
                .append(") )(?<").append(PREFIX_YEARS).append(">\\d+)|(?<").append(SUFFIX_YEARS).append(">\\d+)(").append(yearPrefix)
                .append(")))?) (?<").append(TYPE).append(">\\b(").append(eve).append("|").append(typeAfter).append("|")
                .append(typeBefore).append("|").append(typeOn).append(")\\b)");

        return new GroupPattern(Pattern.compile(patternBuilder.toString()), OFFSET_TIME_PATTERN_GROUPS);
    }

    private String getDayOfWeekPattern(Locale locale) {
        StringBuilder pattern = new StringBuilder();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            pattern.append(dayOfWeekService.getFullDisplayNamePattern(locale, dayOfWeek)).append("|");
        }

        pattern.append(Arrays.stream(DayOfWeek.values()).map(dayOfWeek -> {
            return dayOfWeek.getDisplayName(TextStyle.SHORT, locale);
        }).collect(Collectors.joining("|")));

        return pattern.toString();
    }
}
