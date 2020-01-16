package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface NavigableBotCommand extends MyBotCommand {

    String getHistoryName();

    default void restore(long chatId) {

    }

    default ReplyKeyboardMarkup getKeyboard(long chatId) {
        throw new UnsupportedOperationException();
    }

    default void leave(long chatId) {

    }
}
