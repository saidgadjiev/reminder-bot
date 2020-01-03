package ru.gadjini.reminder.bot.command.api;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.RequestParams;

public interface NavigableCallbackBotCommand {

    String getHistoryName();

    default void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {

    }
}
