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
    public ReplyKeyboardMarkup getSuggestionsKeyboard(long chatId, List<String> suggestions) {
        return storeKeyboard(chatId, keyboardService.getSuggestionsKeyboard(chatId, suggestions));
    }

    @Override
    public ReplyKeyboardMarkup getFriendRequestsKeyboard(long chatId) {
        return storeKeyboard(chatId, keyboardService.getFriendRequestsKeyboard(chatId));
    }

    @Override
    public ReplyKeyboardMarkup getUserSettingsKeyboard(long chatId) {
        return storeKeyboard(chatId, keyboardService.getUserSettingsKeyboard(chatId));
    }

    @Override
    public ReplyKeyboard removeKeyboard(long chatId) {
        ReplyKeyboard replyKeyboard = keyboardService.removeKeyboard(chatId);

        storeKeyboard(chatId, new ReplyKeyboardMarkup());

        return replyKeyboard;
    }

    @Override
    public ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard(long chatId) {
        return storeKeyboard(chatId, keyboardService.getUserReminderNotificationSettingsKeyboard(chatId));
    }

    @Override
    public ReplyKeyboardMarkup getPostponeMessagesKeyboard(long chatId) {
        return storeKeyboard(chatId, keyboardService.getPostponeMessagesKeyboard(chatId));
    }

    @Override
    public ReplyKeyboardMarkup getMainMenu(long chatId, int userId) {
        return storeKeyboard(chatId, keyboardService.getMainMenu(chatId, userId));
    }

    @Override
    public ReplyKeyboardMarkup goBackCommand(long chatId) {
        return storeKeyboard(chatId, keyboardService.goBackCommand(chatId));
    }

    @Override
    public ReplyKeyboardMarkup postponeTimeKeyboard(long chatId) {
        return storeKeyboard(chatId, keyboardService.postponeTimeKeyboard(chatId));
    }

    public ReplyKeyboardMarkup getCurrentReplyKeyboard(long chatId) {
        return replyKeyboardDao.get(chatId);
    }

    private ReplyKeyboardMarkup storeKeyboard(long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        replyKeyboardDao.store(chatId, replyKeyboardMarkup);

        return replyKeyboardMarkup;
    }
}
