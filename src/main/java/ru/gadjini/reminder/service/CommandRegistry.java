package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;

import java.util.*;

@Service
public class CommandRegistry {

    public static final String COMMAND_ARG_SEPARATOR = "_";

    private final Map<String, BotCommand> botCommandRegistryMap = new HashMap<>();

    private final Map<String, CallbackBotCommand> callbackBotCommandMap = new HashMap<>();

    private Collection<KeyboardBotCommand> keyboardBotCommands = new ArrayList<>();

    private CommandNavigator commandNavigator;

    @Autowired
    public CommandRegistry(Collection<BotCommand> botCommands,
                           Collection<CallbackBotCommand> callbackBotCommands,
                           Collection<KeyboardBotCommand> keyboardBotCommands,
                           CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;

        this.keyboardBotCommands.addAll(keyboardBotCommands);
        botCommands.forEach(botCommand -> botCommandRegistryMap.put(botCommand.getCommandIdentifier(), botCommand));
        callbackBotCommands.forEach(callbackBotCommand -> callbackBotCommandMap.put(callbackBotCommand.getName(), callbackBotCommand));
    }

    public BotCommand getBotCommand(String startCommandName) {
        return botCommandRegistryMap.get(startCommandName);
    }

    public boolean isCommand(Message message) {
        if (message.isCommand()) {
            return true;
        }

        return keyboardBotCommands.stream().anyMatch(keyboardBotCommand -> keyboardBotCommand.canHandle(message.getText()));
    }

    public void processNonCommandUpdate(AbsSender absSender, Message message) {
        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(message.getChatId());

        if (navigableBotCommand != null) {
            navigableBotCommand.processNonCommandUpdate(message);
        }
    }

    public boolean executeCommand(AbsSender absSender, Message message) {
        if (message.isCommand()) {
            return executeBotCommand(absSender, message);
        } else {
            return executeKeyBoardCommand(absSender, message);
        }
    }

    public void executeCallbackCommand(AbsSender absSender, CallbackQuery callbackQuery) {
        String text = callbackQuery.getData();
        String[] commandSplit = text.split(COMMAND_ARG_SEPARATOR);
        CallbackBotCommand botCommand = callbackBotCommandMap.get(commandSplit[0]);

        String[] parameters = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);

        botCommand.processMessage(callbackQuery, parameters);
    }

    private boolean executeBotCommand(AbsSender absSender, Message message) {
        String text = message.getText().trim();
        String[] commandSplit = text.split(COMMAND_ARG_SEPARATOR);
        BotCommand botCommand = botCommandRegistryMap.get(commandSplit[0].substring(1));

        if (botCommand != null) {
            String[] parameters = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);

            botCommand.processMessage(absSender, message, parameters);

            if (botCommand instanceof NavigableBotCommand) {
                commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
            }

            return true;
        }

        return false;
    }

    private boolean executeKeyBoardCommand(AbsSender absSender, Message message) {
        String command = message.getText();
        KeyboardBotCommand botCommand = keyboardBotCommands.stream()
                .filter(keyboardBotCommand -> keyboardBotCommand.canHandle(command))
                .findFirst()
                .orElseThrow();

        botCommand.processMessage(message);

        if (botCommand instanceof NavigableBotCommand) {
            commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
        }

        return true;
    }
}
