package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;

public class AcceptFriendRequestCommand implements CallbackBotCommand {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void processMessage(AbsSender absSender, CallbackQuery callbackQuery, String[] arguments) {

    }
}
