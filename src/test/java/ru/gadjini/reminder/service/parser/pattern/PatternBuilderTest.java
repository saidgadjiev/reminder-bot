package ru.gadjini.reminder.service.parser.pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class PatternBuilderTest {

    @Test
    public void test1() {
        Pattern p = Pattern.compile("((((?<minutes>\\d+)мин)?( )?(((?<hours>\\d+)ч)|(час))?( )?)|(?<everyminute>минуту)|(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?( )?(в )?((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)|((?<everyday>день)|((?<days>\\d+)дня))))) кажд[а-я]{0,2}");
        String str = "Тест каждые 2дня";
        String reverse = StringUtils.reverseDelimited(str, ' ');
        Matcher matcher = p.matcher(reverse);

        Assert.assertTrue(matcher.find());
        Assert.assertEquals("2", matcher.group("days"));
    }

    @Test
    public void test2() {
        Pattern p = Pattern.compile("((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?( )?(в )?((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)( (?<nextweek>следующ(ий|ей|ую|ее)|след))?( (во|в))?( )?)?((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )?(((?<month>1[0-2]|[1-9])\\.)?(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>завтра|послезавтра))?");
        String str = "Тест понедельник";
        String reverse = StringUtils.reverseDelimited(str, ' ');
        Matcher matcher = p.matcher(reverse);

        Assert.assertTrue(matcher.find());
        Assert.assertEquals("понедельник", matcher.group("dayofweek"));
    }

    @Test
    public void test3() {
        Pattern p = Pattern.compile("(((?<type>через|за) )?(((?<days>\\d+)д)|(?<eve>накануне))?( )?((?<hours>\\d+)ч)?( )?((?<minutes>\\d+)мин)?)( )?(в )?((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?");
        String str = "за 2д в 19:00";
        Matcher matcher = p.matcher(str);

        System.out.println(matcher.matches());
        System.out.println(matcher.group("days"));
    }
}