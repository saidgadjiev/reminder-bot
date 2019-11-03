package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.common.MessagesProperties;

public interface NavigableBotCommand extends MyBotCommand {

    String getHistoryName();

    default String getParentHistoryName() {
        return MessagesProperties.START_COMMAND_NAME;
    }

    default void restore(long chatId) {

    }

    default ReplyKeyboardMarkup silentRestore() {
        throw new UnsupportedOperationException();
    }
}
