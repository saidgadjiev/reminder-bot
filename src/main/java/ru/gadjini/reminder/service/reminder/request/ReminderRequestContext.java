package ru.gadjini.reminder.service.reminder.request;

import org.telegram.telegrambots.meta.api.objects.User;

import java.time.ZoneId;
import java.util.Locale;

public class ReminderRequestContext {

    private boolean voice;

    private String text;

    private Integer receiverId;

    private ZoneId receiverZoneId;

    private Locale creatorLocale;

    private int messageId;

    private User creator;

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

    public Locale creatorLocale() {
        return this.creatorLocale;
    }

    public int messageId() {
        return this.messageId;
    }

    public User creator() {
        return this.creator;
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

    public ReminderRequestContext creatorLocale(final Locale locale) {
        this.creatorLocale = locale;
        return this;
    }

    public ReminderRequestContext messageId(final int messageId) {
        this.messageId = messageId;
        return this;
    }

    public ReminderRequestContext creator(final User user) {
        this.creator = user;
        return this;
    }
}
