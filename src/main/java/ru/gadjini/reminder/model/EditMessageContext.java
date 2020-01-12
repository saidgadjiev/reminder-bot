package ru.gadjini.reminder.model;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class EditMessageContext {

    private long chatId;

    private int messageId;

    private String text;

    private InlineKeyboardMarkup replyKeyboard;

    public long chatId() {
        return this.chatId;
    }

    public int messageId() {
        return this.messageId;
    }

    public String text() {
        return this.text;
    }

    public InlineKeyboardMarkup replyKeyboard() {
        return this.replyKeyboard;
    }

    public EditMessageContext chatId(final long chatId) {
        this.chatId = chatId;
        return this;
    }

    public EditMessageContext messageId(final int messageId) {
        this.messageId = messageId;
        return this;
    }

    public EditMessageContext text(final String text) {
        this.text = text;
        return this;
    }

    public EditMessageContext replyKeyboard(final InlineKeyboardMarkup replyKeyboard) {
        this.replyKeyboard = replyKeyboard;
        return this;
    }

    public boolean hasKeyboard() {
        return replyKeyboard != null;
    }

    public static EditMessageContext from(CallbackQuery callbackQuery) {
        return new EditMessageContext().chatId(callbackQuery.getMessage().getChatId()).messageId(callbackQuery.getMessage().getMessageId());
    }
}
