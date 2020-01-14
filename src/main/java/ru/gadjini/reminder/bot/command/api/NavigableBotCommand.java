package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.common.CommandNames;

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
