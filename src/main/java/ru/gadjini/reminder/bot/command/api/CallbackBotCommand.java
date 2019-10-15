package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface CallbackBotCommand extends MyBotCommand {

    String getName();

    void processMessage(AbsSender absSender, Message message, String[] arguments);
}
