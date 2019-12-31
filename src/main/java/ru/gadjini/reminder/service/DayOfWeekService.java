package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DayOfWeekService {

    private Map<String, Pattern> patterns = new HashMap<>();

    public DayOfWeekService() {
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            String pattern = getFullDisplayNamePattern(dayOfWeek);
            patterns.put(pattern, Pattern.compile(pattern));
        }
    }

    public String getFullDisplayNamePattern(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case THURSDAY:
                return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) + "[а]?";
            case FRIDAY:
            case SATURDAY:
            case WEDNESDAY: {
                String displayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
                displayName = displayName.substring(0, displayName.length() - 1);
                return displayName + "[ыу]?";
            }
            case SUNDAY:
                String displayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
                displayName = displayName.substring(0, displayName.length() - 1);
                return displayName + "[яе]?";
        }

        throw new UnsupportedOperationException();
    }

    public boolean isThatDay(DayOfWeek dayOfWeek, Locale locale, String value) {
        Pattern pattern = patterns.get(getFullDisplayNamePattern(dayOfWeek));

        return pattern.matcher(value).matches()
                || dayOfWeek.getDisplayName(TextStyle.SHORT, locale).equals(value);
    }

    public List<String> getDayOfWeekSpeechPhrases() {
        List<String> phrases = new ArrayList<>();

        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            String displayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
            phrases.add(displayName);

            switch (dayOfWeek) {
                case FRIDAY:
                case SATURDAY:
                case WEDNESDAY:
                    phrases.add(displayName.substring(0, displayName.length() - 1) + "у");
                    break;
            }
        }

        return phrases;
    }
}
