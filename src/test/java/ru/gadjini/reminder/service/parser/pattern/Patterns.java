package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Patterns {

    static final Pattern FIXED_TIME_PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?(((((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс) ?)((?<nextweek>следующ(ий|ей|ую|ее)|след) ?)?)(во|в ?)?)|((((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )|(((?<year>\\d{4})\\.)?(?<month>1[0-2]|[1-9])\\.))((?<day>0[1-9]|[12]\\d|3[01]|0?[1-9]) ?))|(?<dayword>\\b(сегодня|завтра|послезавтра)\\b))?((?<type>\\bдо\\b) ?)?");
    static final Pattern OFFSET_TIME_PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?((((мин|минут) )?(?<minutes>\\d+)((мин|минут)( )?)?)?((( )?(ч|час[а-я]{0,2}) )?(?<hours>\\d+)((ч|час[а-я]{1,2})( )?)?)?((( )?(д|дн[а-я]{1,2}) )?(?<days>\\d+)(д|дн[а-я]{1,2})?)?) (?<type>\\b(через|на|за|накануне)\\b)");
    static final Pattern REPEAT_TIME_PATTERN = Pattern.compile("(((?<hour>2[0-3]|[01]?[0-9]):(?<minute>[0-5]?[0-9]) ?)(в ?)?)?(((((мин|минут) )?(?<minutes>\\d+)((мин|минут)( )?)?)?((( )?(ч|час[а-я]{0,2}) )?(?<hours>\\d+)((ч|час[а-я]{1,2})( )?)?)?((( )?(д|дн[а-я]{1,2}) )?(?<days>\\d+)(д|дн[а-я]{1,2})?)?)|(?<everyhour>час)|(?<everyday>день)|(((числа )?(?<everymonthday>\\d+)(числа)?) ((?<everymonth>месяц)|((м|месяц[а-я]{0,2} )?(?<months>\\d+)(м|месяц[а-я]{0,2})?)))|((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) (?<day>\\d+)( (?<everyyear>год))?)|(?<everyminute>минуту)|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)) кажд[а-я]{0,2}");

    static int match(Pattern p, String text, Map<String, String> expected) {
        String[] words = text.split(" ");
        StringBuilder toMatch = new StringBuilder();
        Matcher maxMatcher = null;
        for (int i = words.length - 1; i >= 0; --i) {
            if (toMatch.length() > 0) {
                toMatch.append( " ");
            }
            toMatch.append(words[i]);
            Matcher matcher = p.matcher(toMatch);

            if (matcher.find()) {
                maxMatcher = matcher;
            }
        }
        Assert.assertNotNull(maxMatcher);
        for (String group : expected.keySet()) {
            String g = maxMatcher.group(group);

            Assert.assertEquals(expected.get(group), g);
        }

        return text.length() - maxMatcher.end();
    }
}
