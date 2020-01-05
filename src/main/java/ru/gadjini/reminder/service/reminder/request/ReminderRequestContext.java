package ru.gadjini.reminder.service.reminder.request;

import org.telegram.telegrambots.meta.api.objects.User;

import java.time.ZoneId;

public class ReminderRequestContext {

    private boolean voice;

    private String text;

    private String receiverName;

    private Integer receiverId;

    private ZoneId receiverZone;

    private int messageId;

    private User user;

    public boolean isVoice() {
        return voice;
    }

    public ReminderRequestContext setVoice(boolean voice) {
        this.voice = voice;
        return this;
    }

    public String getText() {
        return text;
    }

    public ReminderRequestContext setText(String text) {
        this.text = text;
        return this;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public ReminderRequestContext setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    public ZoneId getReceiverZone() {
        return receiverZone;
    }

    public ReminderRequestContext setReceiverZone(ZoneId receiverZone) {
        this.receiverZone = receiverZone;

        return this;
    }

    public int getMessageId() {
        return messageId;
    }

    public ReminderRequestContext setMessageId(int messageId) {
        this.messageId = messageId;

        return this;
    }

    public User getUser() {
        return user;
    }

    public ReminderRequestContext setUser(User user) {
        this.user = user;

        return this;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public ReminderRequestContext setReceiverName(String receiverName) {
        this.receiverName = receiverName;

        return this;
    }
}
