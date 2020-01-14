package ru.gadjini.reminder.model;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.reminder.job.PriorityJob;

public class EditMessageContext {

    private long chatId;

    private int messageId;

    private String text;

    private InlineKeyboardMarkup replyKeyboard;

    private PriorityJob.Priority priority;

    public EditMessageContext(PriorityJob.Priority priority) {
        this.priority = priority;
    }

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

    public PriorityJob.Priority priority() {
        return priority;
    }

    public static EditMessageContext from(CallbackQuery callbackQuery) {
        return new EditMessageContext(PriorityJob.Priority.MEDIUM).chatId(callbackQuery.getMessage().getChatId()).messageId(callbackQuery.getMessage().getMessageId());
    }
}
