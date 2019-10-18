package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackBotCommand extends MyBotCommand {

    String getName();

    void processMessage(CallbackQuery callbackQuery, String[] arguments);
}
