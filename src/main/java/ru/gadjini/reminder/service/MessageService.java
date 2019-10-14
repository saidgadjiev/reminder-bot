package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
public class MessageService {

    private LocalisationService localisationService;

    @Autowired
    public MessageService(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public void sendAction(AbsSender absSender, long chatId) {
        SendChatAction chatAction = new SendChatAction();

        chatAction.setAction(ActionType.UPLOADAUDIO);
        chatAction.setChatId(chatId);

        try {
            absSender.execute(chatAction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String sendAudio(AbsSender absSender, long chatId, String audio, String fileCaption) {
        SendAudio sendAudio = new SendAudio();

        sendAudio.setAudio(audio);
        sendAudio.setChatId(chatId);
        sendAudio.setTitle(fileCaption);
        sendAudio.setCaption(fileCaption);

        try {
            Message message = absSender.execute(sendAudio);

            return message.getAudio().getFileId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(AbsSender absSender, long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        try {
            absSender.execute(deleteMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(AbsSender absSender, long chatId, String message, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText(message);

        if (replyKeyboard != null) {
            sendMessage.setReplyMarkup(replyKeyboard);
        }

        try {
            absSender.execute(sendMessage);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void sendMessageByCode(AbsSender absSender, long chatId, String messageCode) {
        sendMessage(absSender, chatId, localisationService.getMessage(messageCode), null);
    }

    public void sendMessageByCode(AbsSender absSender, long chatId, String messageCode, Object[] args) {
        sendMessage(absSender, chatId, localisationService.getMessage(messageCode, args), null);
    }
}
