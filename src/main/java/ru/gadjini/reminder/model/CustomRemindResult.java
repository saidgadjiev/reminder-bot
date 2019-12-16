package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.RepeatTime;

import java.time.ZonedDateTime;

public class CustomRemindResult {

    private ZonedDateTime zonedDateTime;

    private RepeatTime repeatTime;

    private ZonedDateTime lastRemindAt;

    private ReminderNotification reminderNotification;

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    public RepeatTime getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(RepeatTime repeatTime) {
        this.repeatTime = repeatTime;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public ZonedDateTime getLastRemindAt() {
        return lastRemindAt;
    }

    public void setLastRemindAt(ZonedDateTime lastRemindAt) {
        this.lastRemindAt = lastRemindAt;
    }

    public boolean isRepeat() {
        return repeatTime != null;
    }

    public boolean isStandard() {
        return zonedDateTime != null;
    }

    public ReminderNotification getReminderNotification() {
        return reminderNotification;
    }

    public void setReminderNotification(ReminderNotification reminderNotification) {
        this.reminderNotification = reminderNotification;
    }
}
