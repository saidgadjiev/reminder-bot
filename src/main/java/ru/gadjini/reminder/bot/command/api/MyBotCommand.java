package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MyBotCommand {

    default void processNonCommandUpdate(Message message) {
    }
}
