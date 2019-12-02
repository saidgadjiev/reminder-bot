package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.command.CallbackCommandNavigator;
import ru.gadjini.reminder.service.command.CommandNavigator;

import java.util.Arrays;

public class GoBackCallbackCommand implements CallbackBotCommand {

    private CallbackCommandNavigator callbackCommandNavigator;

    private CommandNavigator commandNavigator;

    public GoBackCallbackCommand(CallbackCommandNavigator callbackCommandNavigator, CommandNavigator commandNavigator) {
        this.callbackCommandNavigator = callbackCommandNavigator;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public String getName() {
        return MessagesProperties.GO_BACK_CALLBACK_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        String prevCommandName = arguments[0];
        ReplyKeyboard replyKeyboard = null;

        if (Boolean.parseBoolean(arguments[1])) {
            replyKeyboard = commandNavigator.silentPop(callbackQuery.getMessage().getChatId());
        }
        callbackCommandNavigator.goTo(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getId(),
                prevCommandName,
                replyKeyboard,
                Arrays.copyOfRange(arguments, 2, arguments.length)
        );
    }
}
