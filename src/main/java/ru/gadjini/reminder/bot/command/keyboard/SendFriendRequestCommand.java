package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;

public class SendFriendRequestCommand implements KeyboardBotCommand {

    @Override
    public boolean canHandle(String command) {
        return false;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message) {

    }
}
