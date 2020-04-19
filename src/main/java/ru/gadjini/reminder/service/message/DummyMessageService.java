package ru.gadjini.reminder.service.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;

@Service
@Profile(BotConfiguration.PROFILE_TEST)
public class DummyMessageService implements MessageService {

    private Message dummyMessage;

    {
        try {
            dummyMessage = new ObjectMapper().readValue("{ \"message_id\": 12421412 }", Message.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendAction(long chatId, ActionType action) {

    }

    @Override
    public void deleteMessage(long chatId, int messageId) {

    }

    @Override
    public void sendMessageAsync(SendMessageContext messageContext, Consumer<Message> callback) {
        callback.accept(dummyMessage);
    }

    @Override
    public void sendMessageAsync(SendMessageContext messageContext) {

    }

    @Override
    public void sendMessage(SendMessageContext messageContext, Consumer<Message> callback) {

    }

    @Override
    public void sendAnswerCallbackQuery(AnswerCallbackContext callbackContext) {

    }

    @Override
    public void editMessageAsync(EditMessageContext messageContext) {

    }

    @Override
    public void editMessage(EditMessageContext messageContext) {

    }

    @Override
    public void editReplyKeyboard(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard) {

    }

    @Override
    public void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale, Throwable ex) {

    }

    @Override
    public void sendErrorMessage(long chatId, Locale locale, Throwable ex) {

    }

    @Override
    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale) {

    }

    @Override
    public void removeMessageKeyboard(long chatId, int messageId) {

    }
}
