package ru.gadjini.reminder.service.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.configuration.BotConfiguration;

import java.io.IOException;
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
    public void sendAction(long chatId) {

    }

    @Override
    public void deleteMessage(long chatId, int messageId) {

    }

    @Override
    public void sendMessage(long chatId, String message, ReplyKeyboard replyKeyboard, Consumer<Message> callback) {
        callback.accept(dummyMessage);
    }

    @Override
    public void sendMessage(long chatId, String message, ReplyKeyboard replyKeyboard) {
    }

    @Override
    public void sendMessage(long chatId, String message) {
    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode) {

    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode, ReplyKeyboard replyKeyboard) {

    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode, Object[] args) {

    }

    @Override
    public void sendMessageByCode(long chatId, String messageCode, Object[] args, ReplyKeyboard replyKeyboard) {
    }

    @Override
    public void sendAnswerCallbackQuery(String callbackQueryId, String text) {

    }

    @Override
    public void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup replyKeyboard) {

    }

    @Override
    public void editMessage(long chatId, int messageId, String text) {

    }

    @Override
    public void editReplyKeyboard(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard) {

    }

    @Override
    public void editMessageByMessageCode(long chatId, int messageId, String messageCode, InlineKeyboardMarkup keyboardMarkup) {

    }

    @Override
    public void editMessageByMessageCode(long chatId, int messageId, String messageCode, Object[] args, InlineKeyboardMarkup keyboardMarkup) {

    }

    @Override
    public void sendAnswerCallbackQueryByMessageCode(String callbackQueryId, String messageCode) {

    }

    @Override
    public void sendErrorMessage(long chatId, ReplyKeyboard replyKeyboard) {

    }

    @Override
    public void sendErrorMessage(long chatId) {

    }

    @Override
    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard) {

    }
}
