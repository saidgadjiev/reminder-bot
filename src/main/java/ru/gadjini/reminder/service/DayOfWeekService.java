package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DayOfWeekService {

    private final LocalisationService localisationService;

    private Map<String, Map<String, Pattern>> patternsByLocale = new HashMap<>();

    @Autowired
    public DayOfWeekService(LocalisationService localisationService) {
        this.localisationService = localisationService;
        for (Locale locale : localisationService.getSupportedLocales()) {
            Map<String, Pattern> patterns = new HashMap<>();
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                String pattern = getFullDisplayNamePattern(locale, dayOfWeek);
                patterns.put(pattern, Pattern.compile(pattern));
            }
            patternsByLocale.put(locale.getLanguage(), patterns);
        }
    }

    public String getFullDisplayNamePattern(Locale locale, DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case THURSDAY:
                return dayOfWeek.getDisplayName(TextStyle.FULL, locale) + "[а-я]?";
            case FRIDAY:
            case SATURDAY:
            case SUNDAY:
            case WEDNESDAY: {
                String displayName = dayOfWeek.getDisplayName(TextStyle.FULL, locale);
                displayName = displayName.substring(0, displayName.length() - 1);
                return displayName + "[[а-я][а-я]]?";
            }
        }

        throw new UnsupportedOperationException();
    }

    public boolean isThatDay(DayOfWeek dayOfWeek, String value, Locale locale) {
        Pattern pattern = patternsByLocale.get(locale.getLanguage()).get(getFullDisplayNamePattern(locale, dayOfWeek));

        return pattern.matcher(value).matches()
                || dayOfWeek.getDisplayName(TextStyle.SHORT, locale).equals(value);
    }

    public List<String> getDayOfWeekSpeechPhrases() {
        List<String> phrases = new ArrayList<>();

        for (Locale locale : localisationService.getSupportedLocales()) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                String displayName = dayOfWeek.getDisplayName(TextStyle.FULL, locale);
                phrases.add(displayName);

                switch (dayOfWeek) {
                    case FRIDAY:
                    case SATURDAY:
                    case WEDNESDAY:
                        phrases.add(displayName.substring(0, displayName.length() - 1) + "у");
                        break;
                }
            }
        }

        return phrases;
    }
}
