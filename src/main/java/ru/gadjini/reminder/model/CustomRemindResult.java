package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.time.RepeatTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class CustomRemindResult {

    private Reminder reminder;

    private ZonedDateTime zonedDateTime;

    private List<RepeatTime> repeatTimes;

    private List<ReminderNotification> reminderNotifications;

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    public List<RepeatTime> getRepeatTimes() {
        return repeatTimes;
    }

    public void setRepeatTimes(List<RepeatTime> repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public boolean isRepeat() {
        return repeatTimes != null;
    }

    public boolean isStandard() {
        return zonedDateTime != null;
    }

    public List<ReminderNotification> getReminderNotifications() {
        return reminderNotifications;
    }

    public void setReminderNotifications(List<ReminderNotification> reminderNotifications) {
        this.reminderNotifications = reminderNotifications;
    }

    public ZoneId getReceiverZoneId() {
        return reminder.getReceiverZoneId();
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }
}
