package ru.gadjini.reminder.service.command;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.dao.command.navigator.keyboard.CommandNavigatorDao;
import ru.gadjini.reminder.util.ReflectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class CommandNavigator {

    private Map<String, NavigableBotCommand> navigableBotCommands = new HashMap<>();

    private CommandNavigatorDao navigatorDao;

    private CallbackCommandNavigator callbackCommandNavigator;

    @Autowired
    public CommandNavigator(@Qualifier("redis") CommandNavigatorDao navigatorDao) {
        this.navigatorDao = navigatorDao;
    }

    @Autowired
    public void setCallbackCommandNavigator(CallbackCommandNavigator callbackCommandNavigator) {
        this.callbackCommandNavigator = callbackCommandNavigator;
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
            if (Objects.equals(currCommand.getHistoryName(), navigableBotCommand.getHistoryName())) {
                return;
            }
            currCommand.leave(chatId);
            navigatorDao.pushParent(chatId, currCommand.getHistoryName());
        }

        setCurrentCommand(chatId, navigableBotCommand);
        callbackCommandNavigator.silentPop(chatId);
    }

    public boolean isEmpty(long chatId) {
        return navigatorDao.get(chatId) == null;
    }

    public void pop(long chatId) {
        NavigableBotCommand currentCommand = getCurrentCommand(chatId);
        String parentHistoryName = navigatorDao.popParent(chatId, CommandNames.START_COMMAND_NAME);

        if (StringUtils.isNotBlank(parentHistoryName)) {
            currentCommand.leave(chatId);

            NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

            setCurrentCommand(chatId, parentCommand);
            parentCommand.restore(chatId);
        } else {
            currentCommand.restore(chatId);
        }
    }

    public ReplyKeyboardMarkup silentPop(long chatId) {
        NavigableBotCommand navigableBotCommand = getCurrentCommand(chatId);
        if (navigableBotCommand == null) {
            return null;
        }
        navigableBotCommand.leave(chatId);

        String parentHistoryName = navigatorDao.popParent(chatId, CommandNames.START_COMMAND_NAME);
        NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

        setCurrentCommand(chatId, parentCommand);

        return parentCommand.getKeyboard(chatId);
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

    public void setCurrentCommand(long chatId, String command) {
        navigatorDao.set(chatId, command);
    }

    public boolean isCurrentCommandThat(long chatId, String expectedCommand) {
        String currCommand = navigatorDao.get(chatId);

        if (currCommand == null) {
            return false;
        }

        return currCommand.equals(expectedCommand);
    }

    private void setCurrentCommand(long chatId, NavigableBotCommand navigableBotCommand) {
        navigatorDao.set(chatId, navigableBotCommand.getHistoryName());
    }
}
