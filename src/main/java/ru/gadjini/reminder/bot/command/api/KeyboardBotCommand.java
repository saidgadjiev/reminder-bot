package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface KeyboardBotCommand extends MyBotCommand {

    boolean canHandle(String command);

    void processMessage(AbsSender absSender, Message message);
}
