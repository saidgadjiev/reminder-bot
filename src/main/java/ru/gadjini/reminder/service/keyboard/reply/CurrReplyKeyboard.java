package ru.gadjini.reminder.service.keyboard.reply;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.dao.keyboard.ReplyKeyboardDao;

import java.util.List;

@Service
@Qualifier("currkeyboard")
public class CurrReplyKeyboard implements ReplyKeyboardService {

    private ReplyKeyboardDao replyKeyboardDao;

    private ReplyKeyboardService keyboardService;

    public CurrReplyKeyboard(@Qualifier("inMemory") ReplyKeyboardDao replyKeyboardDao, @Qualifier("keyboard") ReplyKeyboardService keyboardService) {
        this.replyKeyboardDao = replyKeyboardDao;
        this.keyboardService = keyboardService;
    }

    @Override
    public ReplyKeyboardMarkup getSavedQueriesKeyboard(long chatId, List<String> queries) {
        return setCurrentKeyboard(chatId, keyboardService.getSavedQueriesKeyboard(chatId, queries));
    }

    @Override
    public ReplyKeyboardMarkup getFriendRequestsKeyboard(long chatId) {
        return setCurrentKeyboard(chatId, keyboardService.getFriendRequestsKeyboard(chatId));
    }

    @Override
    public ReplyKeyboardMarkup getUserSettingsKeyboard(long chatId) {
        return setCurrentKeyboard(chatId, keyboardService.getUserSettingsKeyboard(chatId));
    }

    @Override
    public ReplyKeyboard removeKeyboard(long chatId) {
        ReplyKeyboard replyKeyboard = keyboardService.removeKeyboard(chatId);

        setCurrentKeyboard(chatId, new ReplyKeyboardMarkup());

        return replyKeyboard;
    }

    @Override
    public ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard(long chatId) {
        return setCurrentKeyboard(chatId, keyboardService.getUserReminderNotificationSettingsKeyboard(chatId));
    }

    @Override
    public ReplyKeyboardMarkup getFriendsMenu(long chatId) {
        return setCurrentKeyboard(chatId, keyboardService.getFriendsMenu(chatId));
    }

    @Override
    public ReplyKeyboardMarkup getMainMenu(long chatId, int userId) {
        return setCurrentKeyboard(chatId, keyboardService.getMainMenu(chatId, userId));
    }

    @Override
    public ReplyKeyboardMarkup goBackCommand(long chatId) {
        return setCurrentKeyboard(chatId, keyboardService.goBackCommand(chatId));
    }

    public ReplyKeyboardMarkup getCurrentReplyKeyboard(long chatId) {
        return replyKeyboardDao.get(chatId);
    }

    public ReplyKeyboardMarkup setCurrentKeyboard(long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        replyKeyboardDao.store(chatId, replyKeyboardMarkup);

        return replyKeyboardMarkup;
    }
}
