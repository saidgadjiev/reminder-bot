package ru.gadjini.reminder.model;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class SendMessageContext {

    private long chatId;

    private String text;

    private ReplyKeyboard replyKeyboard;

    public long chatId() {
        return this.chatId;
    }

    public String text() {
        return this.text;
    }

    public ReplyKeyboard replyKeyboard() {
        return this.replyKeyboard;
    }

    public SendMessageContext chatId(final long chatId) {
        this.chatId = chatId;
        return this;
    }

    public SendMessageContext text(final String text) {
        this.text = text;
        return this;
    }

    public SendMessageContext replyKeyboard(final ReplyKeyboard replyKeyboard) {
        this.replyKeyboard = replyKeyboard;
        return this;
    }

    public boolean hasKeyboard() {
        return replyKeyboard != null;
    }
}
