package ru.gadjini.reminder.service.command;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommandNavigator {

    private Map<String, NavigableBotCommand> navigableBotCommands = new HashMap<>();

    private ConcurrentHashMap<Long, NavigableBotCommand> currentCommand = new ConcurrentHashMap<>();

    public void setCommandContainer(CommandContainer commandContainer) {
        Collection<NavigableBotCommand> commands = navigableBotCommands(
                commandContainer.getKeyboardBotCommands(),
                commandContainer.getBotCommandRegistryMap().values(),
                commandContainer.getCallbackBotCommandMap().values()
        );

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

    private Collection<NavigableBotCommand> navigableBotCommands(Collection<KeyboardBotCommand> keyboardBotCommands,
                                                                 Collection<BotCommand> botCommands,
                                                                 Collection<CallbackBotCommand> callbackBotCommands) {
        List<NavigableBotCommand> navigableBotCommands = new ArrayList<>();

        keyboardBotCommands.stream()
                .filter(botCommand -> botCommand instanceof NavigableBotCommand)
                .forEach(botCommand -> navigableBotCommands.add((NavigableBotCommand) botCommand));

        botCommands.stream()
                .filter(botCommand -> botCommand instanceof NavigableBotCommand)
                .forEach(botCommand -> navigableBotCommands.add((NavigableBotCommand) botCommand));

        callbackBotCommands.stream()
                .filter(botCommand -> botCommand instanceof NavigableBotCommand)
                .forEach(botCommand -> navigableBotCommands.add((NavigableBotCommand) botCommand));

        return navigableBotCommands;
    }
}
