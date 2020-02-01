package ru.gadjini.reminder.service.parser.reminder.parser;

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.gadjini.reminder.common.TestConstants;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderLexem;
import ru.gadjini.reminder.service.parser.reminder.lexer.ReminderToken;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalisationService.class, DayOfWeekService.class, TimeCreator.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class ReminderRequestParserTest {

    @Autowired
    private LocalisationService localisationService;

    @Autowired
    private DayOfWeekService dayOfWeekService;

    @Autowired
    private TimeCreator timeCreator;

    @Test
    void fixedTime() {
        ReminderRequestParser parser = new ReminderRequestParser(localisationService, Locale.getDefault(), TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
        ReminderRequest request = parser.parse(lexems(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "января"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));

        Assert.assertEquals(request.getText(), "Тест");
        Assert.assertTrue(request.getTime().isFixedTime());

        LocalDate date = timeCreator.localDateNow(TestConstants.TEST_ZONE).withDayOfMonth(25).withMonth(Month.JANUARY.getValue());
        if (date.isBefore(timeCreator.localDateNow(TestConstants.TEST_ZONE))) {
            date = date.plusYears(1);
        }
        Assert.assertEquals(date, request.getTime().getFixedDateTime().date());
        Assert.assertEquals(TestConstants.TEST_ZONE, request.getZone());
        Assert.assertEquals(LocalTime.of(19, 30), request.getFixedTime().time());
    }

    @Test
    void repeatTime() {
        ReminderRequestParser parser = new ReminderRequestParser(localisationService, Locale.getDefault(), TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
        ReminderRequest request = parser.parse(lexems(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(TimeToken.DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "января"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));

        Assert.assertEquals(request.getText(), "Тест");
        Assert.assertTrue(request.getTime().isRepeatTime());

        Assert.assertEquals(25, request.getTime().getRepeatTimes().get(0).getDay());
        Assert.assertEquals(Month.JANUARY, request.getTime().getRepeatTimes().get(0).getMonth());
        Assert.assertEquals(TestConstants.TEST_ZONE, request.getZone());
        Assert.assertEquals(LocalTime.of(19, 30), request.getTime().getRepeatTimes().get(0).getTime());
    }

    @Test
    void repeatTimes() {
        ReminderRequestParser parser = new ReminderRequestParser(localisationService, Locale.getDefault(), TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
        ReminderRequest request = parser.parse(lexems(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.REPEAT, ""), new TimeLexem(TimeToken.DAY, "25"), new TimeLexem(TimeToken.MONTH_WORD, "января"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30"), new TimeLexem(TimeToken.DAY_OF_WEEK, "среду"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));

        Assert.assertEquals(request.getText(), "Тест");
        Assert.assertTrue(request.getTime().isRepeatTime());

        Assert.assertEquals(25, request.getTime().getRepeatTimes().get(0).getDay());
        Assert.assertEquals(Month.JANUARY, request.getTime().getRepeatTimes().get(0).getMonth());
        Assert.assertEquals(TestConstants.TEST_ZONE, request.getZone());
        Assert.assertEquals(LocalTime.of(19, 30), request.getTime().getRepeatTimes().get(0).getTime());

        Assert.assertEquals(DayOfWeek.WEDNESDAY, request.getTime().getRepeatTimes().get(1).getDayOfWeek());
        Assert.assertEquals(LocalTime.of(19, 30), request.getTime().getRepeatTimes().get(1).getTime());
    }

    @Test
    void offsetTime() {
        ReminderRequestParser parser = new ReminderRequestParser(localisationService, Locale.getDefault(), TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
        ReminderRequest request = parser.parse(lexems(new ReminderLexem(ReminderToken.TEXT, "Тест"), new TimeLexem(TimeToken.OFFSET, ""), new TimeLexem(TimeToken.TYPE, "через"), new TimeLexem(TimeToken.YEARS, "2"), new TimeLexem(TimeToken.MONTHS, "2"), new TimeLexem(TimeToken.DAYS, "2"), new TimeLexem(TimeToken.HOUR, "19"), new TimeLexem(TimeToken.MINUTE, "30")));

        Assert.assertEquals(request.getText(), "Тест");
        Assert.assertTrue(request.getTime().isOffsetTime());

        Assert.assertEquals(new Period().withYears(2).withMonths(2).withDays(2), request.getOffsetTime().getPeriod());
        Assert.assertEquals(TestConstants.TEST_ZONE, request.getZone());
        Assert.assertEquals(LocalTime.of(19, 30), request.getOffsetTime().getTime());
    }

    private List<BaseLexem> lexems(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }
}