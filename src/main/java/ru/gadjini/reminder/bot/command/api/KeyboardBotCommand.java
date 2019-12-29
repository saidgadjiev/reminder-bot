package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface KeyboardBotCommand extends MyBotCommand {

    boolean canHandle(String command);

    boolean processMessage(Message message, String text);
}
