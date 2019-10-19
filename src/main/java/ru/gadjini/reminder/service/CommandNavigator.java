package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CommandMemento;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommandNavigator {

    private ConcurrentHashMap<Long, Stack<CommandMemento>> history = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, NavigableBotCommand> currentCommand = new ConcurrentHashMap<>();

    public void push(long chatId, NavigableBotCommand navigableBotCommand) {
        if (currentCommand.containsKey(chatId)) {
            history.putIfAbsent(chatId, new Stack<>());
            Stack<CommandMemento> historyStack = history.get(chatId);
            NavigableBotCommand currCommand = currentCommand.get(chatId);

            if (!currCommand.getHistoryName().equals(navigableBotCommand.getHistoryName())) {
                historyStack.push(currentCommand.get(chatId).save(chatId));
            }
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
        Stack<CommandMemento> historyStack = history.get(chatId);

        if (!historyStack.isEmpty()) {
            CommandMemento commandMemento = history.get(chatId).pop();
            NavigableBotCommand originator = commandMemento.getOriginator();

            originator.restore(commandMemento);
            currentCommand.put(chatId, originator);
        }
    }

    public ReplyKeyboardMarkup silentPop(long chatId) {
        CommandMemento commandMemento = history.get(chatId).pop();
        NavigableBotCommand originator = commandMemento.getOriginator();

        ReplyKeyboardMarkup replyKeyboardMarkup = originator.silentRestore();

        currentCommand.put(chatId, originator);

        return replyKeyboardMarkup;
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
