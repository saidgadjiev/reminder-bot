package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Patterns {

    //TODO:optimized
    /*static final Pattern FIXED_TIME_PATTERN = Pattern.compile("(?>(\\b(?<hour>2[0-3]|[01]?[0-9])(?>:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(?>(?>(?>(?>(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс) ?)(?>(?<nextweek>следующ(?>ий|ей|ую|ее)|след) ?)?)(?>(?>во|в) ?)?)|(?>(?>(?>(?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )|(?>(?>(?<year>\\d{4})\\.)?(?<month>1[0-2]|[1-9])\\.))(?>(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9]) ?))|(?<dayword>\\b(?>сегодня|завтра|послезавтра)\\b))?(?>(?<type>\\bдо\\b) ?)?");
    static final Pattern OFFSET_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(?>:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(((?>(?>минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(?>минут[ы]?|мин)( )?)?(( )?((?<onehour>час)|(?>(?>час[а-я]{0,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(?>час[а-я]{1,2}|ч)( )?))?(( )?((?<oneday>день)|(?>(?>дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(?>дн[а-я]{1,2}|д)))?) (?<type>\\b(?>накануне|через|на|за)\\b)");
    static final Pattern REPEAT_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(?>:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?((((?>(?>минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(?>минут[ы]?|мин)( )?)?(( )?((?>(?>час[а-я]{0,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(?>час[а-я]{1,2}|ч)( )?))?(( )?((?>(?>дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(?>дн[а-я]{1,2}|д)))?)|(?<everyhour>час)|(?<everyday>день)|(((?>числа )?(?<everymonthday>\\d+)(?>числа)?) ((?<everymonth>месяц)|((?>(?>месяц[а-я]{0,2}|м) )?(?<months>\\d+)(месяц[а-я]{0,2}|м)?)))|(?>(?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) (?<day>\\d+)( (?<everyyear>год))?)|(?<everyminute>минуту)|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)) кажд[а-я]{0,2}");*/

    static final Pattern FIXED_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(((((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс) ?)((?<nextweek>следующ(ий|ей|ую|ее)|след) ?)?)((во|в) ?)?)|((((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )|(((?<year>\\d{4})\\.)?(?<month>1[0-2]|[1-9])\\.))((?<day>0[1-9]|[12]\\d|3[01]|0?[1-9]) ?))|(?<dayword>\\b(сегодня|завтра|послезавтра)\\b))?((?<type>\\bдо\\b) ?)?");
    static final Pattern OFFSET_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?((((минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(минут[ы]?|мин)( )?)?(( )?((?<onehour>час)|((час[а-я]{0,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(час[а-я]{1,2}|ч)( )?))?(( )?((?<oneday>день)|((дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(дн[а-я]{1,2}|д)))?) (?<type>\\b(накануне|через|на|за)\\b)");
    static final Pattern REPEAT_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(((((минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(минут[ы]?|мин)( )?)?(( )?(((час[а-я]{0,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(час[а-я]{1,2}|ч)( )?))?(( )?(((дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(дн[а-я]{1,2}|д)))?)|(?<everyhour>час)|(?<everyday>день)|(((числа )?(?<everymonthday>\\d+)(числа)?) ((?<everymonth>месяц)|(((месяц[а-я]{0,2}|м) )?(?<months>\\d+)(месяц[а-я]{0,2}|м)?)))|((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) (?<day>\\d+)( (?<everyyear>год))?)|(?<everyminute>минуту)|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)) кажд[а-я]{0,2}");

    static int match(Pattern p, String text, Map<String, String> expected) {
        String[] words = text.split(" ");
        StringBuilder toMatch = new StringBuilder();
        Matcher maxMatcher = null;
        for (int i = words.length - 1; i >= 0; --i) {
            if (toMatch.length() > 0) {
                toMatch.append(" ");
            }
            toMatch.append(words[i]);
            Matcher matcher = p.matcher(toMatch);

            if (matcher.matches()) {
                maxMatcher = matcher;
            }
        }
        Assert.assertNotNull(maxMatcher);
        for (String group : expected.keySet()) {
            String g = maxMatcher.group(group);

            Assert.assertEquals(group, expected.get(group), g);
        }

        return text.length() - maxMatcher.end();
    }
}
