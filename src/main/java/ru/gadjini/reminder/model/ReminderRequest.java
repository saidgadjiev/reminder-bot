package ru.gadjini.reminder.model;

import java.time.ZonedDateTime;

public class ReminderRequest {

    private String receiverName;

    private int receiverId;

    private String text;

    private ZonedDateTime remindAt;

    private boolean forMe;

    private String note;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ZonedDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(ZonedDateTime remindAt) {
        this.remindAt = remindAt;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public boolean isForMe() {
        return forMe;
    }

    public void setForMe(boolean forMe) {
        this.forMe = forMe;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "ReminderRequest{" +
                "receiverName='" + receiverName + '\'' +
                ", text='" + text + '\'' +
                ", remindAt=" + remindAt +
                '}';
    }
}
