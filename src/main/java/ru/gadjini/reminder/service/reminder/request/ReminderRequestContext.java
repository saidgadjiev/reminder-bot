package ru.gadjini.reminder.service.reminder.request;

import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.domain.TgUser;

import java.time.ZoneId;
import java.util.Locale;

public class ReminderRequestContext {

    private boolean voice;

    private String text;

    private TgUser receiver;

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
        return receiver == null ? null : receiver.getUserId();
    }

    public ZoneId getReceiverZone() {
        return receiver == null ? null : receiver.getZone();
    }

    public Locale getReceiverLocale() {
        return receiver == null ? null : receiver.getLocale();
    }

    public TgUser getReceiver() {
        return this.receiver;
    }

    public ReminderRequestContext setReceiver(final TgUser receiver) {
        this.receiver = receiver;

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
}
