package ru.gadjini.reminder.service.resolver.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.DateService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginTextTimeMatcher implements RequestMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginTextTimeMatcher.class);

    private static final Pattern PATTERN = Pattern.compile("^@([0-9a-zA-Z_]+) (.+) ((2[0-3]|[01]?[0-9]):([0-5]?[0-9]))$");

    private DateService dateService;

    public LoginTextTimeMatcher(DateService dateService) {
        this.dateService = dateService;
    }

    @Override
    public ReminderRequest match(String text) {
        try {
            Matcher matcher = PATTERN.matcher(text);

            if (matcher.matches()) {
                ReminderRequest reminderRequest = new ReminderRequest();

                reminderRequest.setReceiverName(matcher.group(1));
                reminderRequest.setText(matcher.group(2));

                LocalTime localTime = LocalTime.of(Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
                LocalDateTime localDateTime = dateService.currentUserDateToUtcDate(localTime);
                reminderRequest.setRemindAt(localDateTime);

                reminderRequest.setMatchType(MatchType.LOGIN_TEXT_TIME);

                return reminderRequest;
            }

            return null;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public MatchType getType() {
        return MatchType.LOGIN_TEXT_TIME;
    }
}
