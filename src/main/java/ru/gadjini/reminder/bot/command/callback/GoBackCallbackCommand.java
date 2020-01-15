package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;

@Component
public class GoBackCallbackCommand implements CallbackBotCommand {

    private CallbackCommandNavigator callbackCommandNavigator;

    @Autowired
    public void setCallbackCommandNavigator(CallbackCommandNavigator callbackCommandNavigator) {
        this.callbackCommandNavigator = callbackCommandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.GO_BACK_CALLBACK_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        String prevCommandName = requestParams.getString(Arg.PREV_HISTORY_NAME.getKey());

        callbackCommandNavigator.popTo(TgMessage.from(callbackQuery), prevCommandName, requestParams);

        return null;
    }
}
