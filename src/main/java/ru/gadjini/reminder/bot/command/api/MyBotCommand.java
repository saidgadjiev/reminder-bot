package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface MyBotCommand {

    default void processNonCommandUpdate(AbsSender absSender, Message message) { }
}
