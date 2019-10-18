package ru.gadjini.reminder.model;

import java.time.LocalDateTime;

public class ReminderRequest {

    private String receiverName;

    private String text;

    private LocalDateTime remindAt;

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

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalDateTime remindAt) {
        this.remindAt = remindAt;
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
