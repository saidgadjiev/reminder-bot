package ru.gadjini.reminder.service.reminder.request;

import org.telegram.telegrambots.meta.api.objects.User;

import java.time.ZoneId;
import java.util.Locale;

public class ReminderRequestContext {

    private boolean voice;

    private String text;

    private Integer receiverId;

    private ZoneId receiverZoneId;

    private Locale locale;

    private int messageId;

    private User user;

    public boolean voice() {
        return this.voice;
    }

    public String text() {
        return this.text;
    }

    public Integer receiverId() {
        return this.receiverId;
    }

    public ZoneId receiverZoneId() {
        return this.receiverZoneId;
    }

    public Locale locale() {
        return this.locale;
    }

    public int messageId() {
        return this.messageId;
    }

    public User user() {
        return this.user;
    }

    public ReminderRequestContext voice(final boolean voice) {
        this.voice = voice;
        return this;
    }

    public ReminderRequestContext text(final String text) {
        this.text = text;
        return this;
    }

    public ReminderRequestContext receiverId(final Integer receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    public ReminderRequestContext receiverZoneId(final ZoneId zoneId) {
        this.receiverZoneId = zoneId;
        return this;
    }

    public ReminderRequestContext locale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    public ReminderRequestContext messageId(final int messageId) {
        this.messageId = messageId;
        return this;
    }

    public ReminderRequestContext user(final User user) {
        this.user = user;
        return this;
    }
}
