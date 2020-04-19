package ru.gadjini.reminder.service.message;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;

import java.util.Locale;
import java.util.function.Consumer;

public interface MessageService {

    void sendAction(long chatId, ActionType action);

    void deleteMessage(long chatId, int messageId);

    void sendMessageAsync(SendMessageContext messageContext, Consumer<Message> callback);

    void sendMessageAsync(SendMessageContext messageContext);

    void sendMessage(SendMessageContext messageContext, Consumer<Message> callback);

    void sendAnswerCallbackQuery(AnswerCallbackContext callbackContext);

    void editMessageAsync(EditMessageContext messageContext);

    void editMessage(EditMessageContext messageContext);

    void editReplyKeyboard(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard);

    void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale, Throwable ex);

    void sendErrorMessage(long chatId, Locale locale, Throwable ex);

    void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale);

    void removeMessageKeyboard(long chatId, int messageId);
}
