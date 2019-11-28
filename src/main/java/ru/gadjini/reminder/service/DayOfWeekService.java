package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class DayOfWeekService {

    private Map<String, Pattern> patterns = new HashMap<>();

    public DayOfWeekService() {
        String mondayPattern = getFullPatternStr(DayOfWeek.MONDAY);
        patterns.put(mondayPattern, Pattern.compile(mondayPattern));

        String fridayPattern = getFullPatternStr(DayOfWeek.FRIDAY);
        patterns.put(fridayPattern, Pattern.compile(fridayPattern));

        String sundayPattern = getFullPatternStr(DayOfWeek.SUNDAY);
        patterns.put(sundayPattern, Pattern.compile(sundayPattern));
    }

    public String getFullPatternStr(DayOfWeek dayOfWeek) {
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

    public boolean isMatchFullPattern(DayOfWeek dayOfWeek, String value) {
        Pattern pattern = patterns.get(getFullPatternStr(dayOfWeek));

        return pattern.matcher(value).matches();
    }
}
