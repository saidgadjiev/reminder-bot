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

    @Override
    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        try {
            telegramService.execute(deleteMessage);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void sendMessage(long chatId, String message, ReplyKeyboard replyKeyboard, Consumer<Message> callback) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText(message);

        if (replyKeyboard != null) {
            sendMessage.setReplyMarkup(replyKeyboard);
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
    public void sendMessage(long chatId, String message, ReplyKeyboard replyKeyboard) {
        sendMessage(chatId, message, replyKeyboard, null);
    }

    @Override
    public void sendMessage(long chatId, String message) {
        sendMessage(chatId, message, null, null);
    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode) {
        sendMessage(chatId, localisationService.getMessage(messageCode), null);
    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode, ReplyKeyboard replyKeyboard) {
        sendMessage(chatId, localisationService.getMessage(messageCode), replyKeyboard);
    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode, Object[] args) {
        sendMessage(chatId, localisationService.getMessage(messageCode, args), null);
    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode, Object[] args, ReplyKeyboard replyKeyboard) {
        sendMessage(chatId, localisationService.getMessage(messageCode, args), replyKeyboard, null);
    }

    @Override
    public void sendAnswerCallbackQuery(String callbackQueryId, String text) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();

        answerCallbackQuery.setText(text);
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);

        try {
            telegramService.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
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
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void editMessage(long chatId, int messageId, String text) {
        editMessage(chatId, messageId, text, null);
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
    public void editMessageByMessageCode(long chatId, int messageId, String messageCode, InlineKeyboardMarkup keyboardMarkup) {
        editMessage(chatId, messageId, localisationService.getMessage(messageCode), keyboardMarkup);
    }

    @Override
    public void editMessageByMessageCode(long chatId, int messageId, String messageCode, Object[] args, InlineKeyboardMarkup keyboardMarkup) {
        editMessage(chatId, messageId, localisationService.getMessage(messageCode, args), keyboardMarkup);
    }

    @Override
    public void sendAnswerCallbackQueryByMessageCode(String callbackQueryId, String messageCode) {
        sendAnswerCallbackQuery(callbackQueryId, localisationService.getMessage(messageCode));
    }

    @Override
    public void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard) {
        sendMessageByCode(chatId, MessagesProperties.MESSAGE_ERROR, replyKeyboard);
    }

    @Override
    public void sendErrorMessage(long chatId) {
        sendErrorMessage(chatId, null);
    }

    @Override
    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard) {
        sendMessageByCode(chatId, MessagesProperties.MESSAGE_BOT_RESTARTED, replyKeyboard);
    }
}
