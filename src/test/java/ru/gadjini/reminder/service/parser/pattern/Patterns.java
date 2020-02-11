package ru.gadjini.reminder.service.parser.pattern;

import org.junit.Assert;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patterns {

    //TODO:optimized
    /*static final Pattern FIXED_TIME_PATTERN = Pattern.compile("(?>(\\b(?<hour>2[0-3]|[01]?[0-9])(?>:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(?>(?>(?>(?>(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс) ?)(?>(?<nextweek>следующ(?>ий|ей|ую|ее)|след) ?)?)(?>(?>во|в) ?)?)|(?>(?>(?>(?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )|(?>(?>(?<year>\\d{4})\\.)?(?<month>1[0-2]|[1-9])\\.))(?>(?<day>0[1-9]|[12]\\d|3[01]|0?[1-9]) ?))|(?<dayword>\\b(?>сегодня|завтра|послезавтра)\\b))?(?>(?<type>\\bдо\\b) ?)?");
    static final Pattern OFFSET_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(?>:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(((?>(?>минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(?>минут[ы]?|мин)( )?)?(( )?((?<onehour>час)|(?>(?>час[а-я]{0,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(?>час[а-я]{1,2}|ч)( )?))?(( )?((?<oneday>день)|(?>(?>дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(?>дн[а-я]{1,2}|д)))?) (?<type>\\b(?>накануне|через|на|за)\\b)");
    static final Pattern REPEAT_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(?>:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?((((?>(?>минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(?>минут[ы]?|мин)( )?)?(( )?((?>(?>час[а-я]{0,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(?>час[а-я]{1,2}|ч)( )?))?(( )?((?>(?>дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(?>дн[а-я]{1,2}|д)))?)|(?<everyhour>час)|(?<everyday>день)|(((?>числа )?(?<everymonthday>\\d+)(?>числа)?) ((?<everymonth>месяц)|((?>(?>месяц[а-я]{0,2}|м) )?(?<months>\\d+)(месяц[а-я]{0,2}|м)?)))|(?>(?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) (?<day>\\d+)( (?<everyyear>год))?)|(?<everyminute>минуту)|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс)) кажд[а-я]{0,2}");*/

    public static final Pattern FIXED_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(((((?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс) ?)((?<nextweek>следующ(ий|ей|ую|ее)|след) ?)?)((во|в) ?)?)|((((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) )|(((?<year>\\d{4})\\.)?(?<month>1[0-2]|0?[1-9])\\.))((?<day>0[1-9]|[12]\\d|3[01]|0?[1-9]) ?))|(?<dayword>\\b(сегодня|завтра|послезавтра)\\b))?((?<type>\\bдо\\b) ?)?");
    public static final Pattern OFFSET_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?((((минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(минут[ы]?|мин)( )?)?(( )?((?<onehour>час)|((час[а-я]{1,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(час[а-я]{1,2}|ч)( )?))?(( )?((?<oneday>день)|((дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(дн[а-я]{1,2}|д)))?(( )?((?<onemonth>месяц)|((месяц[а-я]{1,2}|м) )(?<prefixmonths>\\d+)|(?<suffixmonths>\\d+)(месяц[а-я]{1,2}|м)))?(( )?((?<oneyear>год)|((год[а-я]{1,2}|лет|г) )(?<prefixyears>\\d+)|(?<suffixyears>\\d+)(год[а-я]{1,2}|лет|г)))?) (?<type>\\b(накануне|через|за|на)\\b)");
    public static final Pattern REPEAT_TIME_PATTERN = Pattern.compile("((\\b(?<hour>2[0-3]|[01]?[0-9])(:(?<minute>[0-5]?[0-9]))?\\b ?)(в ?)?)?(((((минут[ы]?|мин) )(?<prefixminutes>\\d+)|(?<suffixminutes>\\d+)(минут[ы]?|мин)( )?)?(( )?(((час[а-я]{1,2}|ч) )(?<prefixhours>\\d+)|(?<suffixhours>\\d+)(час[а-я]{1,2}|ч)( )?))?(( )?(((дн[а-я]{1,2}|д) )(?<prefixdays>\\d+)|(?<suffixdays>\\d+)(дн[а-я]{1,2}|д)))?)|(?<onehour>час)|(?<oneday>день)|(((числ[а-я] )?(?<everymonthday>\\d+)(числ[а-я])?) ((?<onemonth>месяц)|(((месяц[а-я]{1,2}|м) )(?<prefixmonths>\\d+)|(?<suffixmonths>\\d+)(месяц[а-я]{1,2}|м))))|((?<monthword>января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря) (?<day>\\d+)( (?<oneyear>год))?)|(?<oneminute>минуту)|(?<dayofweek>понедельник[а]?|вторник[а]?|сред[ыу]?|четверг[а]?|пятниц[ыу]?|суббот[ыу]?|воскресень[яе]?|пн|вт|ср|чт|пт|сб|вс))");
    public static final Pattern REPEAT_WORD_PATTERN = Pattern.compile("кажд[а-я]{1,2}$");

    static int find(Pattern p, String text) {
        Matcher matcher = p.matcher(text);

        Assert.assertTrue(matcher.find());

        return matcher.end();
    }

    private static Matcher maxMatcher(Pattern p, String text) {
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

        return maxMatcher;
    }

    static int match(Pattern p, String text, Map<String, String> expected) {
        Matcher maxMatcher = maxMatcher(p, text);

        Assert.assertNotNull(text, maxMatcher);
        assertGroups(maxMatcher, expected);

        return text.length() - maxMatcher.end();
    }

    static int repeatTimeMatch(String text, List<Map<String, String>> expected) {
        return repeatMatch(REPEAT_TIME_PATTERN, REPEAT_WORD_PATTERN, text, expected);
    }

    static int repeatMatch(Pattern p, Pattern wordsP, String text, List<Map<String, String>> expected) {
        int index = 0;
        int matchEnd = 0;
        String tmp = text;
        Matcher matcher = maxMatcher(p, text);
        while (matcher != null) {
            assertGroups(matcher, expected.get(index++));
            matchEnd += matcher.end();

            tmp = tmp.substring(0, tmp.length() - matcher.end());
            String trimmed = tmp.trim();
            matchEnd += tmp.length() - trimmed.length();
            tmp = trimmed;

            matcher = maxMatcher(p, tmp);
        }
        matcher = maxMatcher(wordsP, tmp);

        Assert.assertNotNull(matcher);

        matchEnd += matcher.end();

        return text.length() - matchEnd;
    }

    private static void assertGroups(Matcher matcher, Map<String, String> expected) {
        for (String group : expected.keySet()) {
            String g = matcher.group(group);

            Assert.assertEquals(group, expected.get(group), g);
        }
    }
}
