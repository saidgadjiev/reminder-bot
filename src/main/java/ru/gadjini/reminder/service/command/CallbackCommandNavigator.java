package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.util.ReflectionUtils;

import java.util.*;

@Service
public class CallbackCommandNavigator {

    private Map<String, NavigableCallbackBotCommand> navigableBotCommands = new HashMap<>();

    @Autowired
    public void setKeyboardCommands(Collection<KeyboardBotCommand> keyboardCommands) {
        ReflectionUtils.findImplements(keyboardCommands, NavigableCallbackBotCommand.class).forEach(command -> navigableBotCommands.put(command.getHistoryName(), command));
    }

    @Autowired
    public void setCallbackCommands(Collection<CallbackBotCommand> callbackCommands) {
        ReflectionUtils.findImplements(callbackCommands, NavigableCallbackBotCommand.class).forEach(command -> navigableBotCommands.put(command.getHistoryName(), command));
    }

    @Autowired
    public void setBotCommands(Collection<BotCommand> botCommands) {
        ReflectionUtils.findImplements(botCommands, NavigableCallbackBotCommand.class).forEach(command -> navigableBotCommands.put(command.getHistoryName(), command));
    }

    public void goTo(long chatId, int messageId, String queryId, String callbackCommandName, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        NavigableCallbackBotCommand callbackBotCommand = navigableBotCommands.get(callbackCommandName);

        callbackBotCommand.restore(chatId, messageId, queryId, replyKeyboard, requestParams);
    }
}
