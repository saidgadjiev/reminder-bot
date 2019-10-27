package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.Reminder;

public class UpdateReminderResult {

    private Reminder oldReminder;

    private Reminder newReminder;

    public Reminder getOldReminder() {
        return oldReminder;
    }

    public void setOldReminder(Reminder oldReminder) {
        this.oldReminder = oldReminder;
    }

    public Reminder getNewReminder() {
        return newReminder;
    }

    public void setNewReminder(Reminder newReminder) {
        this.newReminder = newReminder;
    }
}
