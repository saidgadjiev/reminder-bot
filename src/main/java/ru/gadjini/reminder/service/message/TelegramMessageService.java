package ru.gadjini.reminder.service.message;

import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.ReminderConstants;
import ru.gadjini.reminder.common.TgConstants;
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.job.MessageSenderJob;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TelegramService;
import ru.gadjini.reminder.util.TextUtils;

import java.util.Locale;
import java.util.function.Consumer;

@Service
@Profile("!" + BotConfiguration.PROFILE_TEST)
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
                sendMessage(messageContext, callback);
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

        sendMessage.setChatId(messageContext.chatId());
        sendMessage.enableHtml(true);
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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
        editMessageText.setChatId(messageContext.chatId());
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
        editMessageReplyMarkup.setChatId(chatId);
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

        sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.LOW)
                .chatId(ReminderConstants.REPORT_CHAT)
                .text(buildErrorMessage(chatId, ex))
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

    private String buildErrorMessage(long chatId, Throwable ex) {
        StringBuilder message = new StringBuilder();

        message.append("<b>Message(").append(chatId).append(")</b>: ").append(ex.getMessage()).append("\n\n")
                .append("<b>Stacktrace</b>: ")
                .append(TextUtils.removeHtmlTags(ExceptionUtils.getStackTrace(ex).substring(0, TgConstants.MAX_MESSAGE_SIZE)));

        return message.toString();
    }
}
