package ru.gadjini.reminder.service.validation;

import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

public class ValidationContext {

    private Reminder reminder;

    private Time time;

    private User currentUser;

    private ReminderRequest reminderRequest;

    public Reminder reminder() {
        return this.reminder;
    }

    public Time time() {
        return this.time;
    }

    public ValidationContext reminder(final Reminder reminder) {
        this.reminder = reminder;
        return this;
    }

    public ValidationContext time(final Time time) {
        this.time = time;
        return this;
    }

    public User currentUser() {
        return this.currentUser;
    }

    public ReminderRequest reminderRequest() {
        return this.reminderRequest;
    }

    public ValidationContext currentUser(final User currentUser) {
        this.currentUser = currentUser;
        return this;
    }

    public ValidationContext reminderRequest(final ReminderRequest reminderRequest) {
        this.reminderRequest = reminderRequest;
        return this;
    }
}
