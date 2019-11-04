package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommandNavigator {

    private Map<String, NavigableBotCommand> navigableBotCommands = new HashMap<>();

    private ConcurrentHashMap<Long, NavigableBotCommand> currentCommand = new ConcurrentHashMap<>();

    public void setNavigableBotCommands(Collection<NavigableBotCommand> commands) {
        commands.forEach(navigableBotCommand -> navigableBotCommands.put(navigableBotCommand.getHistoryName(), navigableBotCommand));
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
