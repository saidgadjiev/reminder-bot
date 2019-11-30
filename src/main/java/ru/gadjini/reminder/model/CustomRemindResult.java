package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.Reminder;

import java.time.ZonedDateTime;

public class CustomRemindResult {

    private ZonedDateTime zonedDateTime;

    private Reminder reminder;

    public CustomRemindResult(ZonedDateTime zonedDateTime, Reminder reminder) {
        this.zonedDateTime = zonedDateTime;
        this.reminder = reminder;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public Reminder getReminder() {
        return reminder;
    }
}
