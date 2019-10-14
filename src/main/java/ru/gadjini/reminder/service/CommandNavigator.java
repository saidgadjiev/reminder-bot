package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CommandMemento;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommandNavigator {

    private ConcurrentHashMap<Long, Stack<CommandMemento>> history = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, NavigableBotCommand> currentCommand = new ConcurrentHashMap<>();

    public void push(AbsSender absSender, long chatId, NavigableBotCommand navigableBotCommand) {
        if (currentCommand.containsKey(chatId)) {
            history.putIfAbsent(chatId, new Stack<>());

            history.get(chatId).add(currentCommand.get(chatId).save(absSender, chatId));
            currentCommand.put(chatId, navigableBotCommand);
        } else {
            currentCommand.put(chatId, navigableBotCommand);
        }
    }

    public boolean isEmpty(long chatId) {
        if (currentCommand.containsKey(chatId)) {
            return false;
        }

        return !history.containsKey(chatId);
    }

    public void pop(long chatId) {
        CommandMemento commandMemento = history.get(chatId).pop();
        NavigableBotCommand originator = commandMemento.getOriginator();

        originator.restore(commandMemento);
        currentCommand.put(chatId, originator);
    }

    public NavigableBotCommand getCurrentCommand(long chatId) {
        return currentCommand.get(chatId);
    }

    public void goTo(long chatId, String historyName) {
        Stack<CommandMemento> stack = history.get(chatId);

        CommandMemento commandMemento = stack.pop();

        while (!commandMemento.getOriginator().getHistoryName().equals(historyName) && !stack.isEmpty()) {
            commandMemento = stack.pop();
        }

        currentCommand.put(chatId, commandMemento.getOriginator());
        commandMemento.getOriginator().restore(commandMemento);
    }

    public void zeroRestore(long chatId, NavigableBotCommand botCommand) {
        currentCommand.put(chatId, botCommand);
    }
}
