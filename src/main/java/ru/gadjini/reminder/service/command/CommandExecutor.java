package ru.gadjini.reminder.service.command;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommandExecutor {

    private Map<String, BotCommand> botCommandMap = new HashMap<>();

    private final Map<String, CallbackBotCommand> callbackBotCommandMap = new HashMap<>();

    private Collection<KeyboardBotCommand> keyboardBotCommands;

    private CommandNavigator commandNavigator;

    private CommandParser commandParser;

    private MessageService messageService;

    private LocalisationService localisationService;

    @Autowired
    public CommandExecutor(CommandNavigator commandNavigator, CommandParser commandParser,
                           MessageService messageService, LocalisationService localisationService) {
        this.commandNavigator = commandNavigator;
        this.commandParser = commandParser;
        this.messageService = messageService;
        this.localisationService = localisationService;
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

    public boolean isKeyboardCommand(String text) {
        return keyboardBotCommands
                .stream()
                .anyMatch(keyboardBotCommand -> keyboardBotCommand.canHandle(text));
    }

    public boolean isCommand(Message message, String text) {
        if (message.isCommand()) {
            return true;
        }

        return isKeyboardCommand(text);
    }

    public void processNonCommandUpdate(Message message, String text) {
        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(message.getChatId());

        if (navigableBotCommand != null && navigableBotCommand.accept(message)) {
            navigableBotCommand.processNonCommandUpdate(message, text);
        }
    }

    public void processNonCommandEditedMessage(Message editedMessage, String text) {
        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(editedMessage.getChatId());

        if (navigableBotCommand != null && navigableBotCommand.accept(editedMessage)) {
            navigableBotCommand.processNonCommandEditedMessage(editedMessage, text);
        }
    }

    public boolean executeCommand(Message message, String text) {
        if (message.isCommand()) {
            return executeBotCommand(message);
        } else {
            return executeKeyBoardCommand(message, text);
        }
    }

    public void executeCallbackCommand(CallbackQuery callbackQuery) {
        CommandParser.CommandParseResult parseResult = commandParser.parseCallbackCommand(callbackQuery);
        CallbackBotCommand botCommand = callbackBotCommandMap.get(parseResult.getCommandName());

        String callbackAnswer = botCommand.processMessage(callbackQuery, parseResult.getRequestParams());
        if (StringUtils.isNotBlank(callbackAnswer)) {
            messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), localisationService.getMessage(callbackAnswer));
        }

        if (botCommand instanceof NavigableBotCommand) {
            commandNavigator.push(callbackQuery.getMessage().getChatId(), (NavigableBotCommand) botCommand);
        }
    }

    private boolean executeBotCommand(Message message) {
        CommandParser.CommandParseResult commandParseResult = commandParser.parseBotCommand(message);
        BotCommand botCommand = botCommandMap.get(commandParseResult.getCommandName());

        if (botCommand != null) {
            botCommand.processMessage(null, message, commandParseResult.getParameters());

            if (botCommand instanceof NavigableBotCommand) {
                commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
            }

            return true;
        }

        return false;
    }

    private boolean executeKeyBoardCommand(Message message, String text) {
        KeyboardBotCommand botCommand = keyboardBotCommands.stream()
                .filter(keyboardBotCommand -> keyboardBotCommand.canHandle(text))
                .findFirst()
                .orElseThrow();

        boolean pushToHistory = botCommand.processMessage(message, text);

        if (pushToHistory) {
            commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
        }

        return true;
    }

    public void executeKeyBoardCommandEditedMessage(Message message, String text) {
        KeyboardBotCommand botCommand = keyboardBotCommands.stream()
                .filter(keyboardBotCommand -> keyboardBotCommand.canHandle(text))
                .findFirst()
                .orElseThrow();

        botCommand.processEditedMessage(message, text);
    }
}
