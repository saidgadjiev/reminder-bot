package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MyBotCommand {

    default ActionType getAction() {
        return null;
    }

    default ActionType getNonCommandUpdateAction() {
        return null;
    }

    default ActionType getNonCommandEditAction() {
        return null;
    }

    default boolean accept(Message message) {
        return message.hasText();
    }

    default void processNonCommandUpdate(Message message, String text) {
    }

    default void processNonCommandEditedMessage(Message editedMessage, String text) {

    }
}
