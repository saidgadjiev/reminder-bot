package ru.gadjini.reminder.domain;

import ru.gadjini.reminder.time.DateTime;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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

    public List<ReminderNotification> getReminderNotifications() {
        return reminderNotifications;
    }

    public void setReminderNotifications(List<ReminderNotification> reminderNotifications) {
        this.reminderNotifications = reminderNotifications;
    }

    public TgUser getReceiver() {
        return receiver;
    }

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

    public boolean isRepeatable() {
        return repeatRemindAt != null;
    }

    public RepeatTime getRepeatRemindAt() {
        return repeatRemindAt;
    }

    public RepeatTime getRepeatRemindAtInReceiverZone() {
        return repeatRemindAt == null ? null : repeatRemindAt.withZone(receiver.getZone());
    }

    public void setRepeatRemindAt(RepeatTime repeatRemindAt) {
        this.repeatRemindAt = repeatRemindAt;
    }

    public boolean isMySelf() {
        return creatorId == receiverId;
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
