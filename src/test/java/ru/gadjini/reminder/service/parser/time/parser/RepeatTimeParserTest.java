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
import ru.gadjini.reminder.service.context.UserContextResolver;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserContextResolver.class, LocalisationService.class, DayOfWeekService.class, TimeCreator.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class RepeatTimeParserTest {

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

        Mockito.doReturn(new Locale("ru")).when(localisationService).getCurrentLocale("ru");
    }

    @Test
    void everyDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "1"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(1, parse.getRepeatTimes().get(0).getInterval().getDays());
    }

    @Test
    void everyDays() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "2"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getDays());
    }

    @Test
    void everyMonth() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "1"), new TimeLexem(DAY, "25"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(1, parse.getRepeatTimes().get(0).getInterval().getMonths());
        Assert.assertEquals(25, parse.getRepeatTimes().get(0).getDay());
    }

    @Test
    void everyDayOfWeek() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY_OF_WEEK, "вторник"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(DayOfWeek.TUESDAY, parse.getRepeatTimes().get(0).getDayOfWeek());
    }

    @Test
    void everyYearMonthDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY, "25"), new TimeLexem(MONTH_WORD, "сентября"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(25, parse.getRepeatTimes().get(0).getDay());
        Assert.assertEquals(Month.SEPTEMBER, parse.getRepeatTimes().get(0).getMonth());
    }


    @Test
    void everyMonths() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MONTHS, "2"), new TimeLexem(DAY, "25"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());
        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getMonths());
        Assert.assertEquals(25, parse.getRepeatTimes().get(0).getDay());
    }

    @Test
    void everyMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(MINUTES, "10")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(10, parse.getRepeatTimes().get(0).getInterval().getMinutes());
    }

    @Test
    void everyHours() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(HOURS, "2")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getHours());
    }

    @Test
    void everyHoursMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(HOURS, "2"), new TimeLexem(MINUTES, "10")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getHours());
        Assert.assertEquals(10, parse.getRepeatTimes().get(0).getInterval().getMinutes());
    }

    @Test
    void everyMonthsDaysHoursMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAYS, "2"), new TimeLexem(HOURS, "2"), new TimeLexem(MINUTES, "20")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getDays());
        Assert.assertEquals(2, parse.getRepeatTimes().get(0).getInterval().getHours());
        Assert.assertEquals(20, parse.getRepeatTimes().get(0).getInterval().getMinutes());
    }

    @Test
    void repeatTimes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(DAY_OF_WEEK, "вторник"), new TimeLexem(HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30"), new TimeLexem(DAY_OF_WEEK, "среду"), new TimeLexem(HOUR, "20"), new TimeLexem(TimeToken.MINUTE, "00")));
        Assert.assertTrue(parse.isRepeatTime());

        Assert.assertEquals(DayOfWeek.TUESDAY, parse.getRepeatTimes().get(0).getDayOfWeek());
        Assert.assertEquals(LocalTime.of(19, 30), parse.getRepeatTimes().get(0).getTime());

        Assert.assertEquals(DayOfWeek.WEDNESDAY, parse.getRepeatTimes().get(1).getDayOfWeek());
        Assert.assertEquals(LocalTime.of(20, 0), parse.getRepeatTimes().get(1).getTime());
    }

    private List<BaseLexem> lexems(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }

    private TimeParser parser() {
        return new TimeParser(localisationService, localisationService.getCurrentLocale("ru"), TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
    }
}