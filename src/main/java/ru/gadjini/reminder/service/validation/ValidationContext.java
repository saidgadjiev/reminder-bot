package ru.gadjini.reminder.service.validation;

import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

import java.time.ZonedDateTime;
import java.util.Locale;

public class ValidationContext {

    private Reminder reminder;

    private Time time;

    private User currentUser;

    private ReminderRequest reminderRequest;

    private ZonedDateTime dateTime;

    private Locale locale;

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

    public ZonedDateTime dateTime() {
        return this.dateTime;
    }

    public ValidationContext dateTime(final ZonedDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public Locale locale() {
        return this.locale;
    }

    public ValidationContext locale(final Locale locale) {
        this.locale = locale;
        return this;
    }

}
