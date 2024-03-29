package ru.gadjini.reminder.service.command;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.*;
import ru.gadjini.reminder.model.AnswerCallbackContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.ai.MessageSenderAI;
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

    private CallbackCommandNavigator callbackCommandNavigator;

    private CommandParser commandParser;

    private MessageService messageService;

    private TgUserService userService;

    private LocalisationService localisationService;

    private MessageSenderAI messageSenderAI;

    @Autowired
    public CommandExecutor(CommandParser commandParser, MessageService messageService, TgUserService userService,
                           LocalisationService localisationService, MessageSenderAI messageSenderAI) {
        this.commandParser = commandParser;
        this.messageService = messageService;
        this.userService = userService;
        this.localisationService = localisationService;
        this.messageSenderAI = messageSenderAI;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Autowired
    public void setCallbackCommandNavigator(CallbackCommandNavigator callbackCommandNavigator) {
        this.callbackCommandNavigator = callbackCommandNavigator;
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

    public boolean isTextCommand(long chatId, String text) {
        return keyboardBotCommands
                .stream()
                .anyMatch(keyboardBotCommand -> keyboardBotCommand.canHandle(chatId, text) && keyboardBotCommand.isTextCommand());
    }

    public BotCommand getBotCommand(String startCommandName) {
        return botCommandMap.get(startCommandName);
    }

    public CallbackBotCommand getCallbackCommand(String commandName) {
        return callbackBotCommandMap.get(commandName);
    }

    public boolean isKeyboardCommand(long chatId, String text) {
        return keyboardBotCommands
                .stream()
                .anyMatch(keyboardBotCommand -> keyboardBotCommand.canHandle(chatId, text) && !keyboardBotCommand.isTextCommand());
    }

    public boolean isBotCommand(Message message) {
        return message.isCommand();
    }

    public void processNonCommandUpdate(Message message, String text) {
        NavigableCallbackBotCommand navigableCallbackBotCommand = callbackCommandNavigator.getCurrentCommand(message.getChatId());

        if (navigableCallbackBotCommand != null) {
            if (navigableCallbackBotCommand.accept(message)) {
                navigableCallbackBotCommand.processNonCommandUpdate(message, text);
            }

            return;
        }

        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(message.getChatId());

        if (navigableBotCommand != null && navigableBotCommand.accept(message)) {
            sendNonCommandUpdateAction(message, navigableBotCommand);
            navigableBotCommand.processNonCommandUpdate(message, text);
        }
    }

    public void processNonCommandEditedMessage(Message editedMessage, String text) {
        NavigableCallbackBotCommand navigableCallbackBotCommand = callbackCommandNavigator.getCurrentCommand(editedMessage.getChatId());

        if (navigableCallbackBotCommand != null) {
            if (navigableCallbackBotCommand.accept(editedMessage)) {
                navigableCallbackBotCommand.processNonCommandEditedMessage(editedMessage, text);
            }

            return;
        }
        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(editedMessage.getChatId());

        if (navigableBotCommand != null && navigableBotCommand.accept(editedMessage)) {
            sendNonCommandEditAction(editedMessage.getChatId(), navigableBotCommand);
            navigableBotCommand.processNonCommandEditedMessage(editedMessage, text);
        }
    }

    public void executeCallbackCommand(CallbackQuery callbackQuery) {
        CommandParser.CommandParseResult parseResult = commandParser.parseCallbackCommand(callbackQuery);
        CallbackBotCommand botCommand = callbackBotCommandMap.get(parseResult.getCommandName());

        sendAction(callbackQuery.getMessage().getChatId(), botCommand);

        try {
            if (botCommand instanceof NavigableCallbackBotCommand) {
                callbackCommandNavigator.push(callbackQuery.getMessage().getChatId(), (NavigableCallbackBotCommand) botCommand);
            }
            String callbackAnswer = botCommand.processMessage(callbackQuery, parseResult.getRequestParams());
            if (StringUtils.isNotBlank(callbackAnswer)) {
                messageService.sendAnswerCallbackQuery(new AnswerCallbackContext().queryId(callbackQuery.getId()).text(localisationService.getMessage(callbackAnswer, userService.getLocale(callbackQuery.getFrom().getId()))));
            }
        } catch (Exception ex) {
            if (botCommand instanceof NavigableCallbackBotCommand) {
                callbackCommandNavigator.silentPop(callbackQuery.getMessage().getChatId());
            }
            throw ex;
        }
    }

    public void executeKeyBoardCommandEditedMessage(Message message, String text) {
        KeyboardBotCommand botCommand = keyboardBotCommands.stream()
                .filter(keyboardBotCommand -> keyboardBotCommand.canHandle(message.getChatId(), text))
                .findFirst()
                .orElseThrow();

        sendAction(message.getChatId(), botCommand);
        botCommand.processEditedMessage(message, text);
    }

    public boolean executeBotCommand(Message message) {
        CommandParser.CommandParseResult commandParseResult = commandParser.parseBotCommand(message);
        BotCommand botCommand = botCommandMap.get(commandParseResult.getCommandName());

        if (botCommand != null) {
            sendAction(message.getChatId(), (MyBotCommand) botCommand);
            botCommand.processMessage(null, message, commandParseResult.getParameters());

            if (botCommand instanceof NavigableBotCommand) {
                commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
            }

            return true;
        }

        return false;
    }

    public void executeKeyBoardCommand(Message message, String text) {
        KeyboardBotCommand botCommand = keyboardBotCommands.stream()
                .filter(keyboardBotCommand -> keyboardBotCommand.canHandle(message.getChatId(), text))
                .findFirst()
                .orElseThrow();

        sendAction(message.getChatId(), botCommand);
        boolean pushToHistory = botCommand.processMessage(message, text);

        if (botCommand instanceof NavigableCallbackBotCommand && ((NavigableCallbackBotCommand) botCommand).isAcquireKeyboard()) {
            callbackCommandNavigator.push(message.getChatId(), (NavigableCallbackBotCommand) botCommand);
        } else if (pushToHistory) {
            commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
        }
    }

    private void sendAction(long chatId, MyBotCommand botCommand) {
        ActionType action = botCommand.getAction();

        if (action != null) {
            messageService.sendAction(chatId, action);
        }
    }

    private void sendNonCommandUpdateAction(Message message, MyBotCommand botCommand) {
        ActionType action = botCommand.getNonCommandUpdateAction();

        if (messageSenderAI.isNeedSendAction(new MessageSenderAI.ExecutionContext().command(true).update(message), action)) {
            messageService.sendAction(message.getChatId(), action);
        }
    }

    private void sendNonCommandEditAction(long chatId, MyBotCommand botCommand) {
        ActionType action = botCommand.getNonCommandEditAction();

        if (action != null) {
            messageService.sendAction(chatId, action);
        }
    }
}
