package ru.gadjini.reminder.service.resolver.matcher;

import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.DateService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTimeRequestMatcher implements RequestMatcher {

    private static final Pattern pattern = Pattern.compile("^(.+) ((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))$");

    private DateService dateService;

    public TextTimeRequestMatcher(DateService dateService) {
        this.dateService = dateService;
    }

    @Override
    public ReminderRequest match(String text) {
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            ReminderRequest reminderRequest = new ReminderRequest();

            reminderRequest.setText(matcher.group(1).trim());

            LocalTime localTime = LocalTime.parse(matcher.group(2));
            LocalDateTime localDateTime = dateService.currentUserDateToUtcDate(localTime);
            reminderRequest.setRemindAt(localDateTime);

            reminderRequest.setMatchType(MatchType.TEXT_TIME);

            return reminderRequest;
        }

        return null;
    }

    @Override
    public MatchType getType() {
        return MatchType.TEXT_TIME;
    }
}
