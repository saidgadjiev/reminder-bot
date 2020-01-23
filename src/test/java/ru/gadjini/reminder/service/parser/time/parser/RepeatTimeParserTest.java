package ru.gadjini.reminder.service.parser.time.parser;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.common.TestConstants;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalisationService.class, DayOfWeekService.class, TimeCreator.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class RepeatTimeParserTest {

    @Autowired
    private LocalisationService localisationService;

    @Autowired
    private DayOfWeekService dayOfWeekService;

    @Autowired
    private TimeCreator timeCreator;

    @Test
    void everyDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "1"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTime().getTime());
        Assert.assertEquals(1, parse.getRepeatTime().getInterval().getDays());
    }

    @Test
    void everyDays() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "2"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTime().getTime());
        Assert.assertEquals(2, parse.getRepeatTime().getInterval().getDays());
    }

    @Test
    void everyMonth() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "1"), new TimeLexem(DAY, "25"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTime().getTime());
        Assert.assertEquals(1, parse.getRepeatTime().getInterval().getMonths());
        Assert.assertEquals(25, parse.getRepeatTime().getDay());
    }

    @Test
    void everyDayOfWeek() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY_OF_WEEK, "вторник"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTime().getTime());
        Assert.assertEquals(DayOfWeek.TUESDAY, parse.getRepeatTime().getDayOfWeek());
    }

    @Test
    void everyYearMonthDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY, "25"), new TimeLexem(MONTH_WORD, "сентября"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTime().getTime());
        Assert.assertEquals(25, parse.getRepeatTime().getDay());
        Assert.assertEquals(Month.SEPTEMBER, parse.getRepeatTime().getMonth());
    }


    @Test
    void everyMonths() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "2"), new TimeLexem(DAY, "25"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTime().getTime());
        Assert.assertEquals(2, parse.getRepeatTime().getInterval().getMonths());
        Assert.assertEquals(25, parse.getRepeatTime().getDay());
    }

    @Test
    void everyMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MINUTES, "10")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(10, parse.getRepeatTime().getInterval().getMinutes());
    }

    @Test
    void everyHours() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(HOURS, "2")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTime().getInterval().getHours());
    }

    @Test
    void everyHoursMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(HOURS, "2"), new TimeLexem(MINUTES, "10")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTime().getInterval().getHours());
        Assert.assertEquals(10, parse.getRepeatTime().getInterval().getMinutes());
    }

    @Test
    void everyMonthsDaysHoursMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "2"), new TimeLexem(HOURS, "2"), new TimeLexem(MINUTES, "20")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTime().getInterval().getDays());
        Assert.assertEquals(2, parse.getRepeatTime().getInterval().getHours());
        Assert.assertEquals(20, parse.getRepeatTime().getInterval().getMinutes());
    }

    private List<BaseLexem> lexems(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }

    private TimeParser parser() {
        return new TimeParser(localisationService, Locale.getDefault(), TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
    }
}