package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.util.ReflectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommandNavigator {

    private Map<String, NavigableBotCommand> navigableBotCommands = new HashMap<>();

    private ConcurrentHashMap<Long, NavigableBotCommand> currentCommand = new ConcurrentHashMap<>();

    @Autowired
    public void setKeyboardCommands(Collection<KeyboardBotCommand> keyboardCommands) {
        ReflectionUtils.findImplements(keyboardCommands, NavigableBotCommand.class).forEach(command -> navigableBotCommands.put(command.getHistoryName(), command));
    }

    @Autowired
    public void setCallbackCommands(Collection<CallbackBotCommand> callbackCommands) {
        ReflectionUtils.findImplements(callbackCommands, NavigableBotCommand.class).forEach(command -> navigableBotCommands.put(command.getHistoryName(), command));
    }

    @Autowired
    public void setBotCommands(Collection<BotCommand> botCommands) {
        ReflectionUtils.findImplements(botCommands, NavigableBotCommand.class).forEach(command -> navigableBotCommands.put(command.getHistoryName(), command));
    }

    public void push(long chatId, NavigableBotCommand navigableBotCommand) {
        currentCommand.put(chatId, navigableBotCommand);
    }

    public boolean isEmpty(long chatId) {
        return !currentCommand.containsKey(chatId);
    }

    public void pop(long chatId) {
        NavigableBotCommand navigableBotCommand = currentCommand.get(chatId);
        String parentHistoryName = navigableBotCommand.getParentHistoryName();
        NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

        currentCommand.put(chatId, parentCommand);
        parentCommand.restore(chatId);
    }

    public ReplyKeyboardMarkup silentPop(long chatId) {
        NavigableBotCommand navigableBotCommand = currentCommand.get(chatId);
        if (navigableBotCommand == null) {
            return null;
        }
        String parentHistoryName = navigableBotCommand.getParentHistoryName();
        NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

        currentCommand.put(chatId, parentCommand);

        return parentCommand.silentRestore();
    }

    public NavigableBotCommand getCurrentCommand(long chatId) {
        return currentCommand.get(chatId);
    }

    public void zeroRestore(long chatId, NavigableBotCommand botCommand) {
        currentCommand.put(chatId, botCommand);
    }
}
