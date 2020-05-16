package ru.gadjini.reminder.service.keyboard.reply;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.dao.keyboard.ReplyKeyboardDao;

import java.util.List;
import java.util.Locale;

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
    public ReplyKeyboardMarkup getSavedQueriesKeyboard(long chatId, List<String> queries, Locale locale) {
        return setCurrentKeyboard(chatId, keyboardService.getSavedQueriesKeyboard(chatId, queries, locale));
    }

    @Override
    public ReplyKeyboardMarkup getFriendRequestsKeyboard(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, keyboardService.getFriendRequestsKeyboard(chatId, locale));
    }

    @Override
    public ReplyKeyboardMarkup getUserSettingsKeyboard(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, keyboardService.getUserSettingsKeyboard(chatId, locale));
    }

    @Override
    public ReplyKeyboard removeKeyboard(long chatId) {
        ReplyKeyboard replyKeyboard = keyboardService.removeKeyboard(chatId);

        setCurrentKeyboard(chatId, new ReplyKeyboardMarkup());

        return replyKeyboard;
    }

    @Override
    public ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, keyboardService.getUserReminderNotificationSettingsKeyboard(chatId, locale));
    }

    @Override
    public ReplyKeyboardMarkup getMainMenu(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, keyboardService.getMainMenu(chatId, locale));
    }

    @Override
    public ReplyKeyboardMarkup goBackCommand(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, keyboardService.goBackCommand(chatId, locale));
    }

    public ReplyKeyboardMarkup getCurrentReplyKeyboard(long chatId) {
        return replyKeyboardDao.get(chatId);
    }

    private ReplyKeyboardMarkup setCurrentKeyboard(long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        replyKeyboardDao.store(chatId, replyKeyboardMarkup);

        return replyKeyboardMarkup;
    }
}
