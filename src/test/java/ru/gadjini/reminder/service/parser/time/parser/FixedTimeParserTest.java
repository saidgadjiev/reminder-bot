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
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.DAY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalisationService.class, DayOfWeekService.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class FixedTimeParserTest {

    @Autowired
    private LocalisationService localisationService;

    @Autowired
    private DayOfWeekService dayOfWeekService;

    @Test
    void hourMinute() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "00")));
        Assert.assertTrue(parse.isFixedTime());

        LocalDate localDate = TimeUtils.localDateNow(TestConstants.TEST_ZONE);
        LocalTime localTime = LocalTime.of(19, 0);
        if (localTime.isBefore(LocalTime.now(TestConstants.TEST_ZONE))) {
            localDate = localDate.plusDays(1);
        }

        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 0));
        Assert.assertEquals(parse.getFixedDateTime().date(), localDate);
    }

    @Test
    void tomorrow() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.DAY_WORD, "завтра"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isFixedTime());
        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 30));
        Assert.assertEquals(parse.getFixedDateTime().date(), TimeUtils.localDateNow(TestConstants.TEST_ZONE).plusDays(1));
    }

    @Test
    void dayAfterTomorrow() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.DAY_WORD, "послезавтра"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isFixedTime());
        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 30));
        Assert.assertEquals(parse.getFixedDateTime().date(), TimeUtils.localDateNow(TestConstants.TEST_ZONE).plusDays(2));
    }

    @Test
    void dayOfWeek() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.DAY_OF_WEEK, "среду"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isFixedTime());
        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 30));

        LocalDate date = TimeUtils.localDateNow(TestConstants.TEST_ZONE);
        date = (LocalDate) TemporalAdjusters.next(DayOfWeek.WEDNESDAY).adjustInto(date);
        Assert.assertEquals(parse.getFixedDateTime().date(), date);
    }

    @Test
    void nextDayOfWeek() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.NEXT_WEEK, "след"), new TimeLexem(TimeToken.DAY_OF_WEEK, "среду"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isFixedTime());
        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 30));

        LocalDate date = TimeUtils.localDateNow(TestConstants.TEST_ZONE);
        date = (LocalDate) TemporalAdjusters.next(DayOfWeek.WEDNESDAY).adjustInto(date);
        Assert.assertEquals(parse.getFixedDateTime().date(), date.plusDays(7));
    }

    @Test
    void dayOfMonth() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "сентября"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isFixedTime());
        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 30));

        LocalDate date = TimeUtils.localDateNow(TestConstants.TEST_ZONE).withDayOfMonth(25).withMonth(Month.SEPTEMBER.getValue());
        if (date.isBefore(TimeUtils.localDateNow(TestConstants.TEST_ZONE))) {
            date = date.plusYears(1);
        }
        Assert.assertEquals(parse.getFixedDateTime().date(), date);
    }

    @Test
    void yearMonthDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.YEAR, "2030"), new TimeLexem(TimeToken.MONTH, "01"), new TimeLexem(DAY, "05"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isFixedTime());
        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 30));

        LocalDate date = LocalDate.of(2030, 1, 5);
        Assert.assertEquals(date, parse.getFixedDateTime().date());
        Assert.assertEquals(LocalTime.of(19, 30), parse.getFixedDateTime().time());
    }

    @Test
    void monthDay() {
        TimeParser timeParser = parser();
        Time parse = timeParser.parse(lexems(new TimeLexem(TimeToken.MONTH, "01"), new TimeLexem(DAY, "05"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));
        Assert.assertTrue(parse.isFixedTime());
        Assert.assertEquals(parse.getFixedDateTime().time(), LocalTime.of(19, 30));

        LocalDate date = TimeUtils.localDateNow(TestConstants.TEST_ZONE).withMonth(1).withDayOfMonth(5);
        if (date.isBefore(TimeUtils.localDateNow(TestConstants.TEST_ZONE))) {
            date = date.plusYears(1);
        }
        Assert.assertEquals(date, parse.getFixedDateTime().date());
        Assert.assertEquals(LocalTime.of(19, 30), parse.getFixedDateTime().time());
    }

    private List<BaseLexem> lexems(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }

    private TimeParser parser() {
        return new TimeParser(localisationService, Locale.getDefault(), TestConstants.TEST_ZONE, dayOfWeekService);
    }
}