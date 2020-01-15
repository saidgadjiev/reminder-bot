package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.command.CommandExecutor;

@Component
public class CallbackDelegate implements CallbackBotCommand {

    private CommandExecutor commandExecutor;

    @Autowired
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public String getName() {
        return CommandNames.CALLBACK_DELEGATE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        String delegateCommand = requestParams.getString(Arg.CALLBACK_DELEGATE.getKey());
        CallbackBotCommand callbackCommand = commandExecutor.getCallbackCommand(delegateCommand);

        callbackCommand.processNonCommandCallback(callbackQuery, requestParams);

        return null;
    }
}
