package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gadjini.reminder.common.MessagesProperties;

@Service
public class MessageService {

    private LocalisationService localisationService;

    private TelegramService telegramService;

    @Autowired
    public MessageService(LocalisationService localisationService, TelegramService telegramService) {
        this.localisationService = localisationService;
        this.telegramService = telegramService;
    }

    public void sendAction(long chatId) {
        SendChatAction chatAction = new SendChatAction();

        chatAction.setAction(ActionType.UPLOADAUDIO);
        chatAction.setChatId(chatId);

        try {
            telegramService.execute(chatAction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        try {
            telegramService.execute(deleteMessage);
        } catch (Exception ignore) {

        }
    }

    public Message sendMessage(long chatId, String message, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText(message);

        if (replyKeyboard != null) {
            sendMessage.setReplyMarkup(replyKeyboard);
        }

        try {
            return telegramService.execute(sendMessage);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void sendMessageByCode(long chatId, String messageCode) {
        sendMessage(chatId, localisationService.getMessage(messageCode), null);
    }

    public void sendMessageByCode(long chatId, String messageCode, ReplyKeyboard replyKeyboard) {
        sendMessage(chatId, localisationService.getMessage(messageCode), replyKeyboard);
    }

    public void sendMessageByCode(long chatId, String messageCode, Object[] args) {
        sendMessage(chatId, localisationService.getMessage(messageCode, args), null);
    }

    public Message sendMessageByCode(long chatId, String messageCode, Object[] args, ReplyKeyboard replyKeyboard) {
        return sendMessage(chatId, localisationService.getMessage(messageCode, args), replyKeyboard);
    }

    public void sendAnswerCallbackQuery(String callbackQueryId, String text) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();

        answerCallbackQuery.setText(text);
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);

        try {
            telegramService.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup replyKeyboard) {
        EditMessageText editMessageText = new EditMessageText();

        editMessageText.setMessageId(messageId);
        editMessageText.enableHtml(true);
        editMessageText.setChatId(chatId);
        editMessageText.setText(text);
        if (replyKeyboard != null) {
            editMessageText.setReplyMarkup(replyKeyboard);
        }

        try {
            telegramService.execute(editMessageText);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void editReplyKeyboard(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setReplyMarkup(replyKeyboard);

        try {
            telegramService.execute(editMessageReplyMarkup);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void sendAnswerCallbackQueryByMessageCode(String callbackQueryId, String messageCode) {
        sendAnswerCallbackQuery(callbackQueryId, localisationService.getMessage(messageCode));
    }

    public void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard) {
        sendMessageByCode(chatId, MessagesProperties.MESSAGE_ERROR, replyKeyboard);
    }
}
