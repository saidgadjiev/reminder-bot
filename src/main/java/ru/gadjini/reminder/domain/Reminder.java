package ru.gadjini.reminder.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Reminder {

    public static final String TYPE = "reminder";

    public static final String ID = "id";

    public static final String TEXT = "reminder_text";

    public static final String CREATOR_ID = "creator_id";

    public static final String RECEIVER_ID = "receiver_id";

    public static final String REMIND_AT = "remind_at";

    private int id;

    private String text;

    private int creatorId;

    private TgUser creator;
    
    private int receiverId;

    private TgUser receiver;

    private LocalDateTime remindAt;

    private LocalDateTime remindAtInCreatorTimeZone;

    private LocalDateTime remindAtInReceiverTimeZone;

    private List<ReminderTime> reminderTimes;

    private RemindMessage remindMessage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalDateTime remindAt) {
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

    public List<ReminderTime> getReminderTimes() {
        return reminderTimes;
    }

    public void setReminderTimes(List<ReminderTime> reminderTimes) {
        this.reminderTimes = reminderTimes;
    }

    public TgUser getReceiver() {
        return receiver;
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

    public LocalDateTime getRemindAtInCreatorTimeZone() {
        return remindAtInCreatorTimeZone;
    }

    public void setRemindAtInCreatorTimeZone(LocalDateTime remindAtInCreatorTimeZone) {
        this.remindAtInCreatorTimeZone = remindAtInCreatorTimeZone;
    }

    public LocalDateTime getRemindAtInReceiverTimeZone() {
        return remindAtInReceiverTimeZone;
    }

    public void setRemindAtInReceiverTimeZone(LocalDateTime remindAtInReceiverTimeZone) {
        this.remindAtInReceiverTimeZone = remindAtInReceiverTimeZone;
    }

    public RemindMessage getRemindMessage() {
        return remindMessage;
    }

    public void setRemindMessage(RemindMessage remindMessage) {
        this.remindMessage = remindMessage;
    }
}
