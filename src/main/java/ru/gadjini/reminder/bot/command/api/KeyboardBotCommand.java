package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface KeyboardBotCommand extends MyBotCommand {

    boolean canHandle(String command);
    
    default boolean isTextCommand() {
        return false;
    }

    boolean processMessage(Message message, String text);

    default void processEditedMessage(Message editedMessage, String text) {

    }
}
