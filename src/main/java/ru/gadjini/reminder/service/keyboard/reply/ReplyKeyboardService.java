package ru.gadjini.reminder.service.keyboard.reply;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.Locale;

public interface ReplyKeyboardService {

    ReplyKeyboardMarkup getSavedQueriesKeyboard(long chatId, List<String> queries, Locale locale);

    ReplyKeyboardMarkup getFriendRequestsKeyboard(long chatId, Locale locale);

    ReplyKeyboardMarkup getUserSettingsKeyboard(long chatId, Locale locale);

    ReplyKeyboard removeKeyboard(long chatId);

    ReplyKeyboardMarkup getUserReminderNotificationSettingsKeyboard(long chatId, Locale locale);

    ReplyKeyboardMarkup getMainMenu(long chatId, Locale locale);

    ReplyKeyboardMarkup goBackCommand(long chatId, Locale locale);

}
