package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.dao.CommandNavigatorDao;
import ru.gadjini.reminder.util.ReflectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommandNavigator {

    private Map<String, NavigableBotCommand> navigableBotCommands = new HashMap<>();

    private CommandNavigatorDao navigatorDao;

    @Autowired
    public CommandNavigator(CommandNavigatorDao navigatorDao) {
        this.navigatorDao = navigatorDao;
    }

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
        NavigableBotCommand currCommand = getCurrentCommand(chatId);

        if (currCommand != null) {
            currCommand.leave(chatId);
        }

        setCurrentCommand(chatId, navigableBotCommand);
    }

    public boolean isEmpty(long chatId) {
        return navigatorDao.get(chatId) == null;
    }

    public void pop(long chatId) {
        NavigableBotCommand navigableBotCommand = getCurrentCommand(chatId);
        navigableBotCommand.leave(chatId);

        String parentHistoryName = navigableBotCommand.getParentHistoryName();
        NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

        setCurrentCommand(chatId, parentCommand);
        parentCommand.restore(chatId);
    }

    public ReplyKeyboardMarkup silentPop(long chatId) {
        NavigableBotCommand navigableBotCommand = getCurrentCommand(chatId);
        if (navigableBotCommand == null) {
            return null;
        }
        navigableBotCommand.leave(chatId);

        String parentHistoryName = navigableBotCommand.getParentHistoryName();
        NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

        setCurrentCommand(chatId, parentCommand);

        return parentCommand.silentRestore();
    }

    public void zeroRestore(long chatId, NavigableBotCommand botCommand) {
        setCurrentCommand(chatId, botCommand);
    }

    public NavigableBotCommand getCurrentCommand(long chatId) {
        String currCommand = navigatorDao.get(chatId);

        if (currCommand == null) {
            return null;
        }

        return navigableBotCommands.get(currCommand);
    }

    private void setCurrentCommand(long chatId, NavigableBotCommand navigableBotCommand) {
        navigatorDao.set(chatId, navigableBotCommand.getHistoryName());
    }
}
