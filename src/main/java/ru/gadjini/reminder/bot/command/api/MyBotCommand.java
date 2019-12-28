package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MyBotCommand {

    default boolean accept(Message message) {
        return message.hasText();
    }

    default void processNonCommandUpdate(Message message, String text) {
    }
}
