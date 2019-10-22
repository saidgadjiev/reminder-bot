package ru.gadjini.reminder.service.resolver.matcher;

import ru.gadjini.reminder.model.ReminderRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginTextTimeMatcher implements Function<String, ReminderRequest> {

    private static final Pattern PATTERN = Pattern.compile("^@([0-9a-zA-Z_]+) (.+) ((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))$");

    @Override
    public ReminderRequest apply(String s) {
        Matcher matcher = PATTERN.matcher(s);

        if (matcher.matches()) {
            ReminderRequest reminderRequest = new ReminderRequest();

            reminderRequest.setReceiverName(matcher.group(1));
            reminderRequest.setText(matcher.group(2));

            LocalTime localTime = LocalTime.parse(matcher.group(3));
            LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
            reminderRequest.setRemindAt(localDateTime);

            return reminderRequest;
        }

        return null;
    }
}
