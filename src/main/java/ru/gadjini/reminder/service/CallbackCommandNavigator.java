package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;

import java.util.*;

@Service
public class CallbackCommandNavigator {

    private Map<String, NavigableCallbackBotCommand> navigableBotCommands = new HashMap<>();

    public void setCommandContainer(CommandContainer commandContainer) {
        Collection<NavigableCallbackBotCommand> commands = navigableBotCommands(
                commandContainer.getKeyboardBotCommands(),
                commandContainer.getBotCommandRegistryMap().values(),
                commandContainer.getCallbackBotCommandMap().values()
        );

        commands.forEach(navigableBotCommand -> navigableBotCommands.put(navigableBotCommand.getHistoryName(), navigableBotCommand));
    }

    public void goTo(long chatId, int messageId, String queryId, String callbackCommandName, String[] arguments) {
        NavigableCallbackBotCommand callbackBotCommand = navigableBotCommands.get(callbackCommandName);

        callbackBotCommand.restore(chatId, messageId, queryId, arguments);
    }

    private Collection<NavigableCallbackBotCommand> navigableBotCommands(Collection<KeyboardBotCommand> keyboardBotCommands,
                                                                         Collection<BotCommand> botCommands,
                                                                         Collection<CallbackBotCommand> callbackBotCommands) {
        List<NavigableCallbackBotCommand> navigableBotCommands = new ArrayList<>();

        keyboardBotCommands.stream()
                .filter(botCommand -> botCommand instanceof NavigableCallbackBotCommand)
                .forEach(botCommand -> navigableBotCommands.add((NavigableCallbackBotCommand) botCommand));

        botCommands.stream()
                .filter(botCommand -> botCommand instanceof NavigableCallbackBotCommand)
                .forEach(botCommand -> navigableBotCommands.add((NavigableCallbackBotCommand) botCommand));

        callbackBotCommands.stream()
                .filter(botCommand -> botCommand instanceof NavigableCallbackBotCommand)
                .forEach(botCommand -> navigableBotCommands.add((NavigableCallbackBotCommand) botCommand));

        return navigableBotCommands;
    }
}
