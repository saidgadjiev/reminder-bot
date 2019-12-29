package ru.gadjini.reminder.service.reminder.request;

import java.time.ZoneId;

public class ReminderRequestContext {

    private boolean voice;

    private String text;

    private Integer receiverId;

    private ZoneId receiverZone;

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
}
