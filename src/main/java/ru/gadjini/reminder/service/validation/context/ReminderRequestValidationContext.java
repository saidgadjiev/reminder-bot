package ru.gadjini.reminder.service.validation.context;

import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

public class ReminderRequestValidationContext implements ValidationContext {

    private ReminderRequest reminderRequest;

    public ReminderRequest reminderRequest() {
        return this.reminderRequest;
    }

    public ReminderRequestValidationContext reminderRequest(final ReminderRequest reminderRequest) {
        this.reminderRequest = reminderRequest;
        return this;
    }
}
