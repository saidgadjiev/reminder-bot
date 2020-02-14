package ru.gadjini.reminder.service.parser.time.parser;

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.common.TestConstants;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexer;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalisationService.class, DayOfWeekService.class, TimeCreator.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class OffsetTimeParserTest {

    private static final Locale LOCALE = new Locale("ru");

    @Autowired
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
    void afterHours() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(HOURS, "2")));
        Assert.assertTrue(parse.isOffsetTime());

        Assert.assertEquals(parse.getOffsetTime().getType(), OffsetTime.Type.AFTER);
        Assert.assertEquals(2, parse.getOffsetTime().getHours());
    }

    @Test
    void afterMinutes() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(MINUTES, "20")));
        Assert.assertTrue(parse.isOffsetTime());

        Assert.assertEquals(parse.getOffsetTime().getType(), OffsetTime.Type.AFTER);
        Assert.assertEquals(20, parse.getOffsetTime().getMinutes());
    }

    @Test
    void afterDays() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(DAYS, "2"), new TimeLexem(HOUR, "19"), new TimeLexem(MINUTE, "30")));
        Assert.assertTrue(parse.isOffsetTime());

        Assert.assertEquals(LocalTime.of(19, 30), parse.getOffsetTime().getTime());
        Assert.assertEquals(parse.getOffsetTime().getType(), OffsetTime.Type.AFTER);
        Assert.assertEquals(2, parse.getOffsetTime().getDays());
    }

    @Test
    void afterMonths() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(MINUTES, "20")));
        Assert.assertTrue(parse.isOffsetTime());

        Assert.assertEquals(parse.getOffsetTime().getType(), OffsetTime.Type.AFTER);
        Assert.assertEquals(20, parse.getOffsetTime().getMinutes());
    }

    @Test
    void afterYears() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(YEARS, "2"), new TimeLexem(MONTHS, "2"), new TimeLexem(DAYS, "2"), new TimeLexem(HOURS, "2")));
        Assert.assertTrue(parse.isOffsetTime());

        Assert.assertEquals(parse.getOffsetTime().getType(), OffsetTime.Type.AFTER);
        Assert.assertEquals(new Period().withYears(2).withMonths(2).withDays(2).withHours(2), parse.getOffsetTime().getPeriod());
    }

    @Test
    void matchWeeksDayOfWeek() {
        TimeParser timeParser = parser();
        Time time = timeParser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(WEEKS, "1"), new TimeLexem(DAY_OF_WEEK, "пятницу")));
        Assert.assertTrue(time.isOffsetTime());
        Assert.assertEquals(time.getOffsetTime().getPeriod(), new Period().withWeeks(1));
        Assert.assertEquals(time.getOffsetTime().getDayOfWeek(), DayOfWeek.FRIDAY);

        timeParser = parser();
        time = timeParser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(WEEKS, "2"), new TimeLexem(DAY_OF_WEEK, "вторник")));
        Assert.assertTrue(time.isOffsetTime());
        Assert.assertEquals(time.getOffsetTime().getPeriod(), new Period().withWeeks(2));
        Assert.assertEquals(time.getOffsetTime().getDayOfWeek(), DayOfWeek.TUESDAY);
    }

    @Test
    void afterWeeks() {
        TimeParser parser = parser();
        Time time = parser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(WEEKS, "1")));
        Assert.assertTrue(time.isOffsetTime());
        Assert.assertEquals(time.getOffsetTime().getType(), OffsetTime.Type.AFTER);
        Assert.assertEquals(time.getOffsetTime().getPeriod().getWeeks(), 1);

        parser = parser();
        time = parser.parse(lexems(new TimeLexem(OFFSET, ""), new TimeLexem(TYPE, "через"), new TimeLexem(WEEKS, "2"), new TimeLexem(HOUR, "19"), new TimeLexem(MINUTE, "30")));
        Assert.assertTrue(time.isOffsetTime());
        Assert.assertEquals(time.getOffsetTime().getType(), OffsetTime.Type.AFTER);
        Assert.assertEquals(time.getOffsetTime().getPeriod().getWeeks(), 2);
        Assert.assertEquals(time.getOffsetTime().getTime(), LocalTime.of(19, 30));
    }

    private List<BaseLexem> lexems(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }

    private TimeParser parser() {
        return new TimeParser(localisationService, LOCALE, TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
    }
}