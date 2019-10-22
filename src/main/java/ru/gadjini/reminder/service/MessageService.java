package ru.gadjini.reminder.service;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

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

    public String sendAudio(long chatId, String audio, String fileCaption) {
        SendAudio sendAudio = new SendAudio();

        sendAudio.setAudio(audio);
        sendAudio.setChatId(chatId);
        sendAudio.setTitle(fileCaption);
        sendAudio.setCaption(fileCaption);

        try {
            Message message = telegramService.execute(sendAudio);

            return message.getAudio().getFileId();
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
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public void sendAnswerCallbackQueryByMessageCode(String callbackQueryId, String messageCode) {
        sendAnswerCallbackQuery(callbackQueryId, localisationService.getMessage(messageCode));
    }

    public void sendAnswerCallbackQueryByMessageCode(String callbackQueryId, String messageCode, Object[] args) {
        sendAnswerCallbackQuery(callbackQueryId, localisationService.getMessage(messageCode, args));
    }

    public void sendAnswerInlineQuery(String queryId, List<InlineQueryResult> inlineQueryResults) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();

        answerInlineQuery.setInlineQueryId(queryId);
        answerInlineQuery.setResults(inlineQueryResults);
        answerInlineQuery.setCacheTime(0);

        try {
            telegramService.execute(answerInlineQuery);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
