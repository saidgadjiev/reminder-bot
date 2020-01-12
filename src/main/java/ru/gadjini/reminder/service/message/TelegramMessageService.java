package ru.gadjini.reminder.service.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TelegramService;

import java.util.function.Consumer;

@Service
@Profile("!" + BotConfiguration.PROFILE_TEST)
public class TelegramMessageService implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramMessageService.class);

    private LocalisationService localisationService;

    private TelegramService telegramService;

    @Autowired
    public TelegramMessageService(LocalisationService localisationService, TelegramService telegramService) {
        this.localisationService = localisationService;
        this.telegramService = telegramService;
    }

    @Override
    public void sendAction(long chatId, ActionType action) {
        SendChatAction chatAction = new SendChatAction();

        chatAction.setAction(action);
        chatAction.setChatId(chatId);

        try {
            telegramService.execute(chatAction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        try {
            telegramService.execute(deleteMessage);
        } catch (Exception e) {
            LOGGER.error("Error delete message {}", messageId);
        }
    }

    @Override
    public void sendMessage(SendMessageContext messageContext, Consumer<Message> callback) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(messageContext.chatId());
        sendMessage.enableHtml(true);
        sendMessage.setText(messageContext.text());

        if (messageContext.hasKeyboard()) {
            sendMessage.setReplyMarkup(messageContext.replyKeyboard());
        }

        try {
            Message msg = telegramService.execute(sendMessage);

            if (callback != null) {
                callback.accept(msg);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void sendMessage(SendMessageContext messageContext) {
        sendMessage(messageContext, null);
    }

    @Override
    public void sendAnswerCallbackQuery(AnswerCallbackContext callbackContext) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();

        answerCallbackQuery.setText(callbackContext.text());
        answerCallbackQuery.setCallbackQueryId(callbackContext.queryId());

        try {
            telegramService.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            LOGGER.error("Error answer callback {} with text {}", callbackContext.queryId(), callbackContext.text());
        }
    }

    @Override
    public void editMessage(EditMessageContext messageContext) {
        EditMessageText editMessageText = new EditMessageText();

        editMessageText.setMessageId(messageContext.messageId());
        editMessageText.enableHtml(true);
        editMessageText.setChatId(messageContext.chatId());
        editMessageText.setText(messageContext.text());
        if (messageContext.hasKeyboard()) {
            editMessageText.setReplyMarkup(messageContext.replyKeyboard());
        }

        try {
            telegramService.execute(editMessageText);
        } catch (TelegramApiException ex) {
            LOGGER.error("Error edit message {} with text {}", messageContext.messageId(), messageContext.text());
        }
    }

    @Override
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

    @Override
    public void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard) {
        sendMessage(
                new SendMessageContext()
                        .chatId(chatId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_ERROR))
                        .replyKeyboard(replyKeyboard)
        );
    }

    @Override
    public void sendErrorMessage(long chatId) {
        sendErrorMessage(chatId, null);
    }

    @Override
    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard) {
        sendMessage(
                new SendMessageContext()
                        .chatId(chatId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_RESTARTED))
                        .replyKeyboard(replyKeyboard)
        );
    }
}
