package ru.gadjini.reminder.service.resolver.matcher;

import ru.gadjini.reminder.model.ReminderRequest;

public interface RequestMatcher {

    ReminderRequest match(String text);

    MatchType getType();
}
