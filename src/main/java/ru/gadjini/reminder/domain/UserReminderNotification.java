package ru.gadjini.reminder.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;
import java.time.ZoneId;

public class UserReminderNotification {

    public static final String TABLE = "user_reminder_notification";

    public static final String ID = "id";

    public static final String DAYS = "days";

    public static final String HOURS = "hours";

    public static final String MINUTES = "minutes";

    public static final String TIME = "time";

    public static final String USER_ID = "user_id";

    public static final String TYPE = "type";

    private int id;

    private int days;

    private int hours;

    private int minutes;

    private long userId;

    private TgUser user;

    private LocalTime time;

    private NotificationType type;

    private ZoneId zoneId;

    @JsonCreator
    public UserReminderNotification(@JsonProperty("zoneId") ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public TgUser getUser() {
        return user;
    }

    public void setUser(TgUser user) {
        this.user = user;
    }

    public enum NotificationType {
        WITHOUT_TIME(0),
        WITH_TIME(1);

        private final int code;

        NotificationType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static NotificationType fromCode(int code) {
            for (NotificationType notificationType : values()) {
                if (notificationType.code == code) {
                    return notificationType;
                }
            }

            throw new IllegalArgumentException();
        }
    }
}
