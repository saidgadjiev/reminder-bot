package ru.gadjini.reminder.service.message;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;

import java.util.function.Consumer;

public interface MessageService {

    void sendAction(long chatId, ActionType action);

    void deleteMessage(long chatId, int messageId);

    void sendMessage(SendMessageContext messageContext, Consumer<Message> callback);

    void sendMessage(SendMessageContext messageContext);

    void sendAnswerCallbackQuery(AnswerCallbackContext callbackContext);

    void editMessage(EditMessageContext messageContext);

    void editReplyKeyboard(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard);

    void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard);

    void sendErrorMessage(long chatId);

    void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard);
}
