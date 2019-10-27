package ru.gadjini.reminder.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class ReminderTime {

    public static final String TYPE = "reminder_time";

    public static final String ID = "id";

    public static final String FIXED_TIME = "fixed_time";

    public static final String DELAY_TIME = "delay_time";

    public static final String TYPE_COL = "time_type";

    public static final String REMINDER_ID = "reminder_id";

    public static final String LAST_REMINDER_AT = "last_reminder_at";

    private int id;

    private ZonedDateTime fixedTime;

    private LocalTime delayTime;

    private ZonedDateTime lastReminderAt;

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

    public ZonedDateTime getFixedTime() {
        return fixedTime;
    }

    public void setFixedTime(ZonedDateTime fixedTime) {
        this.fixedTime = fixedTime;
    }

    public LocalTime getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(LocalTime delayTime) {
        this.delayTime = delayTime;
    }

    public ZonedDateTime getLastReminderAt() {
        return lastReminderAt;
    }

    public void setLastReminderAt(ZonedDateTime lastReminderAt) {
        this.lastReminderAt = lastReminderAt;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public enum Type {

        ONCE(0),

        REPEAT(1);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public static Type fromCode(int code) {
            for (Type type: values()) {
                if (type.code == code) {
                    return type;
                }
            }

            throw new IllegalArgumentException();
        }

        public int getCode() {
            return code;
        }
    }
}
