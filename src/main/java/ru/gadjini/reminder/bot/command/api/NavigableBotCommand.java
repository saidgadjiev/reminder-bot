package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.bots.AbsSender;

public interface NavigableBotCommand extends MyBotCommand {

    String getHistoryName();

    default CommandMemento save(AbsSender absSender, long chatId) {
        return new DefaultMemento(absSender, chatId, this);
    }

    default void restore(CommandMemento commandMemento) {

    }
}
