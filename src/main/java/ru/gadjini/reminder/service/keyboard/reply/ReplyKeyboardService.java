package ru.gadjini.reminder.service.keyboard.reply;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

public interface ReplyKeyboardService {
    ReplyKeyboardMarkup getSuggestionsKeyboard(long chatId, List<String> suggestions);

    ReplyKeyboardMarkup getFriendRequestsKeyboard(long chatId);

    ReplyKeyboardMarkup getUserSettingsKeyboard(long chatId);

    ReplyKeyboard removeKeyboard(long chatId);

    ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard(long chatId);

    ReplyKeyboardMarkup getPostponeMessagesKeyboard(long chatId);

    ReplyKeyboardMarkup getMainMenu(long chatId, int userId);

    ReplyKeyboardMarkup goBackCommand(long chatId);

    ReplyKeyboardMarkup postponeTimeKeyboard(long chatId);
}
