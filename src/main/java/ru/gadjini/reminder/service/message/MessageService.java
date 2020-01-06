package ru.gadjini.reminder.service.message;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.function.Consumer;

public interface MessageService {

    void sendAction(long chatId, ActionType action);

    void deleteMessage(long chatId, int messageId);

    void sendMessage(long chatId, String message, ReplyKeyboard replyKeyboard, Consumer<Message> callback);

    void sendMessage(long chatId, String message, ReplyKeyboard replyKeyboard);

    void sendMessage(long chatId, String message);

    void sendMessageByCode(long chatId, String messageCode);

    void sendMessageByCode(long chatId, String messageCode, ReplyKeyboard replyKeyboard);

    void sendMessageByCode(long chatId, String messageCode, Object[] args);

    void sendMessageByCode(long chatId, String messageCode, Object[] args, ReplyKeyboard replyKeyboard);

    void sendAnswerCallbackQuery(String callbackQueryId, String text);

    void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup replyKeyboard);

    void editMessage(long chatId, int messageId, String text);

    void editReplyKeyboard(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard);

    void editMessageByMessageCode(long chatId, int messageId, String messageCode, InlineKeyboardMarkup keyboardMarkup);

    void sendAnswerCallbackQueryByMessageCode(String callbackQueryId, String messageCode);

    void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard);

    void sendErrorMessage(long chatId);

    void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard);
}
