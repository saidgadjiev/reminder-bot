package ru.gadjini.reminder.model;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class TgMessage {

    private long chatId;

    private int messageId;

    private String callbackQueryId;

    private User user;

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getCallbackQueryId() {
        return callbackQueryId;
    }

    public void setCallbackQueryId(String callbackQueryId) {
        this.callbackQueryId = callbackQueryId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static TgMessage from(CallbackQuery callbackQuery) {
        TgMessage tgMessage = new TgMessage();

        tgMessage.chatId = callbackQuery.getMessage().getChatId();
        tgMessage.messageId = callbackQuery.getMessage().getMessageId();
        tgMessage.callbackQueryId = callbackQuery.getId();
        tgMessage.user = callbackQuery.getFrom();

        return tgMessage;
    }

    public static TgMessage from(Message message) {
        TgMessage tgMessage = new TgMessage();

        tgMessage.chatId = message.getChatId();
        tgMessage.messageId = message.getMessageId();
        tgMessage.user = message.getFrom();

        return tgMessage;
    }

    public static TgMessage from(Update update) {
        if (update.hasCallbackQuery()) {
            return from(update.getCallbackQuery());
        } else if (update.hasEditedMessage()) {
            return from(update.getEditedMessage());
        }

        return from(update.getMessage());
    }

    public static long getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId();
        }

        return update.getMessage().getChatId();
    }
}
