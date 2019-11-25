package ru.gadjini.reminder.bot.command.api;

public interface NavigableCallbackBotCommand {

    String getHistoryName();

    default void restore(long chatId, int messageId, String queryId, String[] arguments) {

    }
}
