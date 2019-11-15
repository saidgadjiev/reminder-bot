package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;

public class ChangeReminderNoteCommand implements CallbackBotCommand {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {

    }
}
