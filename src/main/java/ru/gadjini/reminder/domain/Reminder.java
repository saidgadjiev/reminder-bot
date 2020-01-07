package ru.gadjini.reminder.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jooq.Field;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Reminder {

    public static final String TYPE = "reminder";

    public static final String ID = "id";

    public static final String TEXT = "reminder_text";

    public static final String CREATOR_ID = "creator_id";

    public static final String RECEIVER_ID = "receiver_id";

    public static final String REMIND_AT = "remind_at";

    public static final String INITIAL_REMIND_AT = "initial_remind_at";

    public static final String COMPLETED_AT = "completed_at";

    public static final String STATUS = "status";

    public static final String NOTE = "note";

    public static final String REPEAT_REMIND_AT = "repeat_remind_at";

    public static final String MESSAGE_ID = "message_id";

    public static final String CURRENT_SERIES = "current_series";

    public static final String MAX_SERIES = "max_series";

    private int id;

    private String text;

    private int creatorId;

    private TgUser creator;

    private int receiverId;

    private TgUser receiver;

    private DateTime remindAt;

    private DateTime initialRemindAt;

    private List<ReminderNotification> reminderNotifications = new ArrayList<>();

    private RemindMessage remindMessage;

    private Status status;

    private String note;

    private RepeatTime repeatRemindAt;

    private ZonedDateTime completedAt;

    private int messageId;

    private int currentSeries;

    private int maxSeries;

    public Reminder() {
    }

    public Reminder(Reminder reminder) {
        this.id = reminder.id;
        this.text = reminder.text;
        this.creatorId = reminder.creatorId;
        this.creator = reminder.creator;
        this.receiverId = reminder.receiverId;
        this.receiver = reminder.receiver;
        this.remindAt = reminder.remindAt;
        this.initialRemindAt = reminder.initialRemindAt;
        this.status = reminder.status;
        this.note = reminder.note;
        this.repeatRemindAt = reminder.repeatRemindAt;
        this.completedAt = reminder.completedAt;
        this.messageId = reminder.messageId;
        this.remindMessage = reminder.remindMessage;
        this.reminderNotifications = reminder.reminderNotifications;
        this.currentSeries = reminder.currentSeries;
        this.maxSeries = reminder.maxSeries;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DateTime getRemindAt() {
        return remindAt;
    }

    @JsonIgnore
    public DateTime getRemindAtInReceiverZone() {
        return remindAt.withZoneSameInstant(receiver.getZone());
    }

    public void setRemindAt(DateTime remindAt) {
        this.remindAt = remindAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    @JsonIgnore
    public List<ReminderNotification> getReminderNotifications() {
        return reminderNotifications;
    }

    public void setReminderNotifications(List<ReminderNotification> reminderNotifications) {
        this.reminderNotifications = reminderNotifications;
    }

    public TgUser getReceiver() {
        return receiver;
    }

    @JsonIgnore
    public ZoneId getReceiverZoneId() {
        return receiver.getZone();
    }

    public void setReceiver(TgUser receiver) {
        this.receiver = receiver;
    }

    public void setCreator(TgUser tgUser) {
        this.creator = tgUser;
    }

    public TgUser getCreator() {
        return creator;
    }

    public RemindMessage getRemindMessage() {
        return remindMessage;
    }

    public void setRemindMessage(RemindMessage remindMessage) {
        this.remindMessage = remindMessage;
    }

    @JsonIgnore
    public boolean hasRemindMessage() {
        return remindMessage != null;
    }

    public DateTime getInitialRemindAt() {
        return initialRemindAt;
    }

    public void setInitialRemindAt(DateTime initialRemindAt) {
        this.initialRemindAt = initialRemindAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @JsonIgnore
    public boolean isRepeatable() {
        return repeatRemindAt != null;
    }

    public RepeatTime getRepeatRemindAt() {
        return repeatRemindAt;
    }

    @JsonIgnore
    public RepeatTime getRepeatRemindAtInReceiverZone() {
        return repeatRemindAt == null ? null : repeatRemindAt.withZone(receiver.getZone());
    }

    public void setRepeatRemindAt(RepeatTime repeatRemindAt) {
        this.repeatRemindAt = repeatRemindAt;
    }

    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    @JsonIgnore
    public ZonedDateTime getCompletedAtInReceiverZone() {
        return completedAt.withZoneSameInstant(receiver.getZone());
    }

    public void setCompletedAt(ZonedDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @JsonIgnore
    public boolean isMySelf() {
        return creatorId == receiverId;
    }

    @JsonIgnore
    public boolean isNotMySelf() {
        return creatorId != receiverId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getCurrentSeries() {
        return currentSeries;
    }

    public void setCurrentSeries(int currentSeries) {
        this.currentSeries = currentSeries;
    }

    public int getMaxSeries() {
        return maxSeries;
    }

    public void setMaxSeries(int maxSeries) {
        this.maxSeries = maxSeries;
    }

    public Map<Field<?>, Object> getDiff(Reminder newReminder) {
        Map<Field<?>, Object> values = new HashMap<>();
        if (!Objects.equals(getText(), newReminder.getText())) {
            values.put(ReminderTable.TABLE.TEXT, newReminder.getText());
        }
        if (!Objects.equals(getNote(), newReminder.getNote())) {
            values.put(ReminderTable.TABLE.NOTE, newReminder.getNote());
        }
        if (!Objects.equals(getRepeatRemindAt(), newReminder.getRepeatRemindAt())) {
            values.put(ReminderTable.TABLE.REPEAT_REMIND_AT, newReminder.getRepeatRemindAt() == null ? null : newReminder.getRepeatRemindAt().sqlObject());
        }
        if (!Objects.equals(getRemindAt(), newReminder.getRemindAt())) {
            values.put(ReminderTable.TABLE.REMIND_AT, newReminder.getRemindAt() == null ? null : newReminder.getRemindAt().sqlObject());
        }

        return values;
    }

    public enum Status {

        ACTIVE(0),
        COMPLETED(1);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }

            throw new IllegalArgumentException();
        }
    }
}
