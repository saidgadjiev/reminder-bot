package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public interface NavigableCallbackBotCommand {

    String getHistoryName();

    default void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, String[] arguments) {

    }
}
