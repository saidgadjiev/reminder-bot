package ru.gadjini.reminder.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.request.RequestParamsParser;
import ru.gadjini.reminder.service.message.MessageTextExtractor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommandExecutor {

    public static final String COMMAND_ARG_SEPARATOR = "=";

    public static final String COMMAND_NAME_SEPARATOR = ":";

    private Map<String, BotCommand> botCommandMap = new HashMap<>();

    private final Map<String, CallbackBotCommand> callbackBotCommandMap = new HashMap<>();

    private Collection<KeyboardBotCommand> keyboardBotCommands;

    private CommandNavigator commandNavigator;

    private RequestParamsParser requestParamsParser;

    private MessageTextExtractor messageTextExtractor;

    @Autowired
    public CommandExecutor(CommandNavigator commandNavigator, RequestParamsParser requestParamsParser, MessageTextExtractor messageTextExtractor) {
        this.commandNavigator = commandNavigator;
        this.requestParamsParser = requestParamsParser;
        this.messageTextExtractor = messageTextExtractor;
    }

    @Autowired
    public void setKeyboardCommands(Collection<KeyboardBotCommand> keyboardCommands) {
        this.keyboardBotCommands = keyboardCommands;
    }

    @Autowired
    public void setCallbackCommands(Collection<CallbackBotCommand> callbackCommands) {
        callbackCommands.forEach(callbackBotCommand -> callbackBotCommandMap.put(callbackBotCommand.getName(), callbackBotCommand));
    }

    @Autowired
    public void setBotCommands(Collection<BotCommand> botCommands) {
        botCommands.forEach(botCommand -> botCommandMap.put(botCommand.getCommandIdentifier(), botCommand));
    }

    public BotCommand getBotCommand(String startCommandName) {
        return botCommandMap.get(startCommandName);
    }

    public boolean isCommand(Message message) {
        if (message.isCommand()) {
            return true;
        }

        return keyboardBotCommands.stream().anyMatch(keyboardBotCommand -> keyboardBotCommand.canHandle(message.getText()));
    }

    public void processNonCommandUpdate(Message message) {
        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(message.getChatId());

        if (navigableBotCommand != null && navigableBotCommand.accept(message)) {
            String text = messageTextExtractor.extract(message);
            navigableBotCommand.processNonCommandUpdate(message, text);
        }
    }

    public boolean executeCommand(AbsSender absSender, Message message) {
        if (message.isCommand()) {
            return executeBotCommand(absSender, message);
        } else {
            return executeKeyBoardCommand(message);
        }
    }

    public void executeCallbackCommand(CallbackQuery callbackQuery) {
        String text = callbackQuery.getData();
        String[] commandSplit = text.split(COMMAND_NAME_SEPARATOR);
        CallbackBotCommand botCommand = callbackBotCommandMap.get(commandSplit[0]);
        RequestParams requestParams = new RequestParams();

        if (commandSplit.length > 1) {
            requestParams = requestParamsParser.parse(commandSplit[1]);
        }
        botCommand.processMessage(callbackQuery, requestParams);

        if (botCommand instanceof NavigableBotCommand) {
            commandNavigator.push(callbackQuery.getMessage().getChatId(), (NavigableBotCommand) botCommand);
        }
    }

    private boolean executeBotCommand(AbsSender absSender, Message message) {
        String text = message.getText().trim();
        String[] commandSplit = text.split(COMMAND_ARG_SEPARATOR);
        BotCommand botCommand = botCommandMap.get(commandSplit[0].substring(1));

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

    private boolean executeKeyBoardCommand(Message message) {
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
