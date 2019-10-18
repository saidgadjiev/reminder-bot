package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface NavigableBotCommand extends MyBotCommand {

    String getHistoryName();

    default CommandMemento save(long chatId) {
        return new DefaultMemento(chatId, this);
    }

    default void restore(CommandMemento commandMemento) {

    }

    default ReplyKeyboardMarkup silentRestore() {
        throw new UnsupportedOperationException();
    }
}
