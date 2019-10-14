package ru.gadjini.reminder.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReminderTime {

    private int id;

    private LocalDateTime fixedTime;

    private LocalTime delayTime;

    private LocalDateTime lastReminderAt;

    private Type type;

    private int reminderId;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getFixedTime() {
        return fixedTime;
    }

    public void setFixedTime(LocalDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    public LocalTime getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(LocalTime delayTime) {
        this.delayTime = delayTime;
    }

    public LocalDateTime getLastReminderAt() {
        return lastReminderAt;
    }

    public void setLastReminderAt(LocalDateTime lastReminderAt) {
        this.lastReminderAt = lastReminderAt;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public enum Type {

        ONCE,

        REPEAT
    }
}
