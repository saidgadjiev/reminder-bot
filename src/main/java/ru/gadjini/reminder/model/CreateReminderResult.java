package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.validation.ErrorBag;

public class CreateReminderResult {

    private ErrorBag errorBag;

    private Reminder reminder;

    public CreateReminderResult(ErrorBag errorBag, Reminder reminder) {
        this.errorBag = errorBag;
        this.reminder = reminder;
    }

    public ErrorBag getErrorBag() {
        return errorBag;
    }

    public Reminder getReminder() {
        return reminder;
    }
}
