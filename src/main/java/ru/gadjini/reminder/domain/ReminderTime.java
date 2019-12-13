package ru.gadjini.reminder.domain;

import org.joda.time.Period;

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

    public static final String ITS_TIME = "its_time";

    private int id;

    private ZonedDateTime fixedTime;

    private Period delayTime;

    private ZonedDateTime lastReminderAt;

    private Type type;

    private int reminderId;

    private boolean itsTime = false;

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

    public Period getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Period delayTime) {
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

    public boolean isItsTime() {
        return itsTime;
    }

    public void setItsTime(boolean itsTime) {
        this.itsTime = itsTime;
    }

    public static ReminderTime repeatTime() {
        return new ReminderTime() {{
            setType(Type.REPEAT);
        }};
    }

    public static ReminderTime onceTime() {
        return new ReminderTime() {{
            setType(Type.ONCE);
        }};
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
