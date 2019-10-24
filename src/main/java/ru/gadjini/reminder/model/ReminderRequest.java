package ru.gadjini.reminder.model;

import ru.gadjini.reminder.service.resolver.matcher.MatchType;

import java.time.LocalDateTime;

public class ReminderRequest {

    private String receiverName;

    private int receiverId;

    private String text;

    private LocalDateTime remindAt;

    private MatchType matchType;

    private boolean forMe;

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

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public boolean isForMe() {
        return forMe;
    }

    public void setForMe(boolean forMe) {
        this.forMe = forMe;
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
