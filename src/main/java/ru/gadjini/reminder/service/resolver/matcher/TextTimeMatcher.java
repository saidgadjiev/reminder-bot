package ru.gadjini.reminder.service.resolver.matcher;

import ru.gadjini.reminder.model.ReminderRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTimeMatcher implements Function<String, ReminderRequest> {

    private static final Pattern pattern = Pattern.compile("^(.+) ((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))$");

    @Override
    public ReminderRequest apply(String s) {
        Matcher matcher = pattern.matcher(s);

        if (matcher.matches()) {
            ReminderRequest reminderRequest = new ReminderRequest();

            reminderRequest.setText(matcher.group(1).trim());

            LocalTime localTime = LocalTime.parse(matcher.group(2));
            LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
            reminderRequest.setRemindAt(localDateTime);

            reminderRequest.setMatchType(MatchType.TEXT_TIME);

            return reminderRequest;
        }

        return null;
    }
}
