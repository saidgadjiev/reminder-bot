package ru.gadjini.reminder.service.parser.time.parser;

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
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.DayOfWeekService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.api.BaseLexem;
import ru.gadjini.reminder.service.parser.pattern.PatternBuilder;
import ru.gadjini.reminder.service.parser.time.lexer.TimeLexem;
import ru.gadjini.reminder.service.parser.time.lexer.TimeToken;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static ru.gadjini.reminder.service.parser.time.lexer.TimeToken.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalisationService.class, DayOfWeekService.class, TimeCreator.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
class OffsetTimeParserTest {

    @Autowired
    private LocalisationService localisationService;

    @Autowired
    private DayOfWeekService dayOfWeekService;

    @Autowired
    private TimeCreator timeCreator;

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

    private List<BaseLexem> lexems(BaseLexem... lexems) {
        return new LinkedList<>(Arrays.asList(lexems));
    }

    private TimeParser parser() {
        return new TimeParser(localisationService, Locale.getDefault(), TestConstants.TEST_ZONE, dayOfWeekService, timeCreator);
    }
}