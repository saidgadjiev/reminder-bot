package ru.gadjini.reminder.service.parser.time.parser;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.common.TestConstants;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.Lexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexer;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static ru.gadjini.reminder.service.parser.time.lexer.RepeatTimeToken.SERIES_TO_COMPLETE;
import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalisationService.class, DayOfWeekService.class, TimeCreator.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class RepeatTimeParserTest {

    private static final Locale LOCALE = new Locale("ru");

    @SpyBean
    private LocalisationService localisationService;

    @Autowired
    private DayOfWeekService dayOfWeekService;

    @MockBean
    private TimeCreator timeCreator;

    @BeforeEach
    void setup() {
        ZonedDateTime STATIC_TIME = ZonedDateTime.of(LocalDate.of(2020, 1, 1), LocalTime.of(11, 0), TestConstants.TEST_ZONE);

        Mockito.when(timeCreator.dateTimeNow(TestConstants.TEST_ZONE)).thenReturn(DateTime.of(STATIC_TIME));
        Mockito.when(timeCreator.zonedDateTimeNow(TestConstants.TEST_ZONE)).thenReturn(STATIC_TIME);
    }

    @Test
    void everyDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(DAYS, "1"), new Lexem(HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(1, parse.getRepeatTimes().get(0).getInterval().getDays());
    }

    @Test
    void everyDays() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(DAYS, "2"), new Lexem(HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getDays());
    }

    @Test
    void everyMonth() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(MONTHS, "1"), new Lexem(DAY, "25"), new Lexem(HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(1, parse.getRepeatTimes().get(0).getInterval().getMonths());
        Assert.assertEquals(25, parse.getRepeatTimes().get(0).getDay());
    }

    @Test
    void everyDayOfWeek() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(DAY_OF_WEEK, "вторник"), new Lexem(HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(DayOfWeek.TUESDAY, parse.getRepeatTimes().get(0).getDayOfWeek());
    }

    @Test
    void everyYearMonthDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(DAY, "25"), new Lexem(MONTH_WORD, "сентября"), new Lexem(HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(25, parse.getRepeatTimes().get(0).getDay());
        Assert.assertEquals(Month.SEPTEMBER, parse.getRepeatTimes().get(0).getMonth());
    }


    @Test
    void everyMonths() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(MONTHS, "2"), new Lexem(DAY, "25"), new Lexem(HOUR, "19"), new Lexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getMonths());
        Assert.assertEquals(25, parse.getRepeatTimes().get(0).getDay());
    }

    @Test
    void everyMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(MINUTES, "10")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(10, parse.getRepeatTimes().get(0).getInterval().getMinutes());
    }

    @Test
    void everyHours() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(HOURS, "2")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getHours());
    }

    @Test
    void everyHoursMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(HOURS, "2"), new Lexem(MINUTES, "10")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getHours());
        Assert.assertEquals(10, parse.getRepeatTimes().get(0).getInterval().getMinutes());
    }

    @Test
    void everyMonthsDaysHoursMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(DAYS, "2"), new Lexem(HOURS, "2"), new Lexem(MINUTES, "20")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getDays());
        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getHours());
        Assert.assertEquals(20, parse.getRepeatTimes().get(0).getInterval().getMinutes());
    }

    @Test
    void afterWeeks() {
        TimeParser timeParser = parser();
        Time time = timeParser.parse(lexems(new Lexem(REPEAT, ""), new Lexem(WEEKS, "1")));
        Assert.assertTrue(time.isRepeatTime());
        Assert.assertEquals(time.getRepeatTimes().get(0).getInterval().getWeeks(), 1);

        timeParser = parser();
        time = timeParser.parse(lexems(new Lexem(REPEAT, ""), new Lexem(WEEKS, "2"), new Lexem(HOUR, "19"), new Lexem(MINUTE, "30")));
        Assert.assertTrue(time.isRepeatTime());
        Assert.assertEquals(time.getRepeatTimes().get(0).getInterval().getWeeks(), 2);
        Assert.assertEquals(LocalTime.of(19, 30), time.getRepeatTimes().get(0).getTime());
    }

    @Test
    void matchWeeksDayOfWeek() {
        TimeParser timeParser = parser();
        Time time = timeParser.parse(lexems(new Lexem(REPEAT, ""), new Lexem(WEEKS, "1"), new Lexem(DAY_OF_WEEK, "вторник")));
        Assert.assertTrue(time.isRepeatTime());
        Assert.assertEquals(time.getRepeatTimes().get(0).getInterval(), new org.joda.time.Period().withWeeks(1));
        Assert.assertEquals(time.getRepeatTimes().get(0).getDayOfWeek(), DayOfWeek.TUESDAY);

        timeParser = parser();
        time = timeParser.parse(lexems(new Lexem(REPEAT, ""), new Lexem(WEEKS, "2"), new Lexem(DAY_OF_WEEK, "субботу")));
        Assert.assertTrue(time.isRepeatTime());
        Assert.assertEquals(time.getRepeatTimes().get(0).getInterval(), new org.joda.time.Period().withWeeks(2));
        Assert.assertEquals(time.getRepeatTimes().get(0).getDayOfWeek(), DayOfWeek.SATURDAY);
    }

    @Test
    void repeatTimes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(TimeToken.REPEAT, ""), new Lexem(DAY_OF_WEEK, "вторник"), new Lexem(HOUR, "19"), new Lexem(TimeToken.MINUTE, "30"), new Lexem(DAY_OF_WEEK, "среду"), new Lexem(HOUR, "20"), new Lexem(TimeToken.MINUTE, "00")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(DayOfWeek.TUESDAY, parse.getRepeatTimes().get(0).getDayOfWeek());
        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());

        Assert.assertEquals(DayOfWeek.WEDNESDAY, parse.getRepeatTimes().get(1).getDayOfWeek());
        Assert.assertEquals(LocalTime.of(20, 0), parse.getRepeatTimes().get(1).getTime());
    }

    @Test
    void matchRepeatWithoutTime() {
        TimeParser parser = parser();
        Time parsed = parser.parse(lexems(new Lexem(REPEAT, "")));
        Assert.assertTrue(parsed.isRepeatTime());

        Assert.assertNull(parsed.getRepeatTimes().get(0).getTime());
    }

    @Test
    void matchSeriesToComplete() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new Lexem(REPEAT, ""), new Lexem(DAYS, "1"), new Lexem(SERIES_TO_COMPLETE, "5"), new Lexem(DAY_OF_WEEK, "вторник"), new Lexem(SERIES_TO_COMPLETE, "4")));
        Assert.assertTrue(parse.isRepeatTime());
        Assert.assertEquals(2, parse.getRepeatTimes().size());

        Assert.assertEquals(1, parse.getRepeatTimes().get(0).getInterval().getDays());
        Assert.assertEquals(5, (int) parse.getRepeatTimes().get(0).getSeriesToComplete());

        Assert.assertEquals(DayOfWeek.TUESDAY, parse.getRepeatTimes().get(1).getDayOfWeek());
        Assert.assertEquals(4, (int) parse.getRepeatTimes().get(1).getSeriesToComplete());
    }

    private List<Lexem> lexems(Lexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }

    private TimeParser parser() {
        return new TimeParser(localisationService, LOCALE, TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
    }
}