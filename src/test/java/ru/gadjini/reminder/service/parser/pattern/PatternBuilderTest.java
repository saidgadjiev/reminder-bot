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
        Pattern p1 = Pattern.compile("кажд[а-я]{0,2} (((((?<hours>\\d+)( )?ч|час{0,2})|(?<everyhour>час))?( )?((?<minutes>\\d+)( )?мин|минут)?( )?)|(?<everyminute>минуту)|(((?<everyday>день)|((?<days>\\d+)( )?д|дня))|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс))( )?(в )?((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?)");
        Pattern p = Pattern.compile("кажд[а-я]{0,2} (((((?<hours>\\d+)( )?ч|час{0,2})|(?<everyhour>час))?( )?((?<minutes>\\d+)( )?мин|минут)?( )?)|(?<everyminute>минуту)|(((?<everyday>день)|((?<days>\\d+)( )?(д|дня)))|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс))( )?(в )?((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?)");
        String str = "каждые 2 дня";
        findMaxMatcher(p, str);
    }

    @Test
    public void test2() {
        Pattern b = Pattern.compile("(((?<month>1[0-2]|[1-9])\\.)?((?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>завтра|послезавтра)) )?((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )?(((во|в) )?( )?((?<nextweek>следующ(ий|ей|ую|ее)|след) )?(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс))?( )?(в )?((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?");

        Pattern p = Pattern.compile("((?<type>до|в) )?(((?<year>\\d{4})\\.)?((?<month>1[0-2]|[1-9])\\.)?((?<day>0[1-9]|[12]\\d|3[01]|0?[1-9])|(?<dayword>завтра|послезавтра|сегодня)))?( )??(?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)?( )?(((во|в) )?( )?((?<nextweek>следующ(ий|ей|ую|ее)|след) )?(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс))?( )?(в )?((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?");
        String str = "2030.9.20";

        findMaxMatcher(p, str);
    }

    @Test
    public void test3() {
        Pattern p = Pattern.compile("((((?<type>через|на|за)|(?<eve>накануне)) )((?<days>\\d+)( )?д|дня)?( )?((?<hours>\\d+)( )?ч|час{0,2})?( )?((?<minutes>\\d+)( )?мин|минут)?)( )?(в )?((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]))?");
        String str = "за 2 ч";

        findMaxMatcher(p, str);
    }

    private void findMaxMatcher(Pattern p, String str) {
        String[] words = str.split(" ");
        StringBuilder toMatch = new StringBuilder();
        Matcher maxMatcher = null;
        for (int i = words.length - 1; i >= 0; --i) {
            if (toMatch.length() > 0) {
                toMatch.insert(0, " ");
            }
            toMatch.insert(0, words[i]);
            Matcher matcher = p.matcher(toMatch.toString());

            if (matcher.matches()) {
                maxMatcher = matcher;
            }
        }
        if (maxMatcher != null) {
            for (int i = 0; i < maxMatcher.groupCount(); ++i) {
                String group = maxMatcher.group(i);

                if (StringUtils.isNotBlank(group)) {
                    System.out.println(group);
                }
            }
        }
    }
}