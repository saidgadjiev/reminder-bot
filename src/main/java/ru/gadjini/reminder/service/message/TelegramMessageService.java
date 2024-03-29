package ru.gadjini.reminder.service.message;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.TgConstants;
import ru.gadjini.reminder.exception.TelegramMethodException;
import ru.gadjini.reminder.job.MessageSenderJob;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TelegramService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Service
public class TelegramMessageService implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramMessageService.class);

    private LocalisationService localisationService;

    private TelegramService telegramService;

    private MessageSenderJob senderJob;

    @Autowired
    public TelegramMessageService(LocalisationService localisationService, TelegramService telegramService, MessageSenderJob senderJob) {
        this.localisationService = localisationService;
        this.telegramService = telegramService;
        this.senderJob = senderJob;
    }

    @Override
    public void sendAction(long chatId, ActionType action) {
        SendChatAction chatAction = new SendChatAction();

        chatAction.setAction(action);
        chatAction.setChatId(String.valueOf(chatId));

        try {
            telegramService.execute(chatAction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);

        try {
            telegramService.execute(deleteMessage);
        } catch (TelegramApiRequestException ex) {
            LOGGER.error(ex.getApiResponse(), ex);
        } catch (TelegramApiException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void sendMessageAsync(SendMessageContext messageContext, Consumer<Message> callback) {
        senderJob.push(new PriorityJob(messageContext.priority()) {
            @Override
            public void run() {
                sendMessageWithTgLimits(messageContext, callback);
            }
        });
    }

    @Override
    public void sendMessageAsync(SendMessageContext messageContext) {
        sendMessageAsync(messageContext, null);
    }

    @Override
    public void sendMessage(SendMessageContext messageContext, Consumer<Message> callback) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(messageContext.chatId()));
        sendMessage.enableHtml(messageContext.html());
        sendMessage.setText(messageContext.text());
        sendMessage.disableWebPagePreview();

        if (messageContext.hasKeyboard()) {
            sendMessage.setReplyMarkup(messageContext.replyKeyboard());
        }

        try {
            Message msg = telegramService.execute(sendMessage);

            if (callback != null) {
                callback.accept(msg);
            }
        } catch (TelegramApiRequestException ex) {
            throw new TelegramMethodException(ex, messageContext.chatId());
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex.getMessage() + "(" + messageContext.chatId() + ")", ex);
        }
    }

    @Override
    public void sendAnswerCallbackQuery(AnswerCallbackContext callbackContext) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();

        answerCallbackQuery.setText(callbackContext.text());
        answerCallbackQuery.setCallbackQueryId(callbackContext.queryId());

        try {
            telegramService.execute(answerCallbackQuery);
        } catch (TelegramApiRequestException ex) {
            LOGGER.error(ex.getApiResponse(), ex);
        } catch (TelegramApiException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void editMessageAsync(EditMessageContext messageContext) {
        senderJob.push(new PriorityJob(messageContext.priority()) {
            @Override
            public void run() {
                editMessage(messageContext);
            }
        });
    }

    @Override
    public void editMessage(EditMessageContext messageContext) {
        EditMessageText editMessageText = new EditMessageText();

        editMessageText.setMessageId(messageContext.messageId());
        editMessageText.enableHtml(true);
        editMessageText.setChatId(String.valueOf(messageContext.chatId()));
        editMessageText.setText(messageContext.text());
        if (messageContext.hasKeyboard()) {
            editMessageText.setReplyMarkup(messageContext.replyKeyboard());
        }

        try {
            telegramService.execute(editMessageText);
        } catch (TelegramApiRequestException ex) {
            LOGGER.error(ex.getApiResponse(), ex);
        } catch (TelegramApiException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void editReplyKeyboard(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setReplyMarkup(replyKeyboard);

        try {
            telegramService.execute(editMessageReplyMarkup);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale, Throwable ex) {
        sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_ERROR, locale))
                        .replyKeyboard(replyKeyboard)
        );
    }

    @Override
    public void sendErrorMessage(long chatId, Locale locale, Throwable ex) {
        sendErrorMessage(chatId, null, locale, ex);
    }

    @Override
    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale) {
        sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_RESTARTED, locale))
                        .replyKeyboard(replyKeyboard)
        );
    }

    @Override
    public void removeMessageKeyboard(long chatId, int messageId) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);

        try {
            telegramService.execute(editMessageReplyMarkup);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void sendMessageWithTgLimits(SendMessageContext sendMessage, Consumer<Message> callback) {
        if (sendMessage.text().length() < TgConstants.MAX_MESSAGE_SIZE) {
            sendMessage(sendMessage, callback);
        } else {
            List<String> parts = new ArrayList<>();
            Splitter.fixedLength(TgConstants.MAX_MESSAGE_SIZE)
                    .split(sendMessage.text())
                    .forEach(parts::add);
            for (int i = 0; i < parts.size() - 1; ++i) {
                SendMessageContext msg = new SendMessageContext(sendMessage.priority()).chatId(sendMessage.chatId())
                        .text(parts.get(i))
                        .html(sendMessage.html());
                sendMessage(msg, null);
            }

            SendMessageContext msg = new SendMessageContext(sendMessage.priority())
                    .chatId(sendMessage.chatId()).text(parts.get(parts.size() - 1))
                    .html(sendMessage.html())
                    .replyKeyboard(sendMessage.replyKeyboard());
            sendMessage(msg, callback);
        }
    }
}
