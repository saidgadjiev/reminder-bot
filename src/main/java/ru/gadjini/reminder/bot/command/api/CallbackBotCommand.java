package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.request.RequestParams;

public interface CallbackBotCommand extends MyBotCommand {

    String getName();

    void processMessage(CallbackQuery callbackQuery, RequestParams requestParams);
}
