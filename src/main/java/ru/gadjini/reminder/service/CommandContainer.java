package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.gadjini.reminder.bot.command.HelpCommand;
import ru.gadjini.reminder.bot.command.StartCommand;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.callback.*;
import ru.gadjini.reminder.bot.command.keyboard.*;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.parser.RequestParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandContainer {

    private final Map<String, BotCommand> botCommandRegistryMap = new HashMap<>();

    private final Map<String, CallbackBotCommand> callbackBotCommandMap = new HashMap<>();

    private Collection<KeyboardBotCommand> keyboardBotCommands;

    public CommandContainer(KeyboardService keyboardService,
                            FriendshipService friendshipService,
                            LocalisationService localisationService,
                            CommandNavigator commandNavigator,
                            ReminderService reminderService,
                            TgUserService tgUserService,
                            RequestParser requestParser,
                            SecurityService securityService,
                            ReminderMessageSender reminderMessageSender,
                            MessageService messageService,
                            CallbackCommandNavigator callbackCommandNavigator,
                            TimezoneService timezoneService) {
        for (BotCommand botCommand : getBotCommands(keyboardService, reminderService, tgUserService, requestParser,
                securityService, reminderMessageSender, messageService)) {
            botCommandRegistryMap.put(botCommand.getCommandIdentifier(), botCommand);
        }
        for (CallbackBotCommand botCommand : getCallbackBotCommands(keyboardService, friendshipService, localisationService,
                commandNavigator, reminderService, tgUserService, requestParser, reminderMessageSender, messageService, callbackCommandNavigator, securityService)) {
            callbackBotCommandMap.put(botCommand.getName(), botCommand);
        }
        keyboardBotCommands = getKeyboardBotCommands(
                keyboardService,
                friendshipService,
                localisationService,
                commandNavigator,
                tgUserService,
                timezoneService,
                messageService
        );

        commandNavigator.setCommandContainer(this);
        callbackCommandNavigator.setCommandContainer(this);
    }

    public Map<String, BotCommand> getBotCommandRegistryMap() {
        return botCommandRegistryMap;
    }

    public Map<String, CallbackBotCommand> getCallbackBotCommandMap() {
        return callbackBotCommandMap;
    }

    public Collection<KeyboardBotCommand> getKeyboardBotCommands() {
        return keyboardBotCommands;
    }

    private List<KeyboardBotCommand> getKeyboardBotCommands(KeyboardService keyboardService,
                                                            FriendshipService friendshipService,
                                                            LocalisationService localisationService,
                                                            CommandNavigator commandNavigator,
                                                            TgUserService tgUserService,
                                                            TimezoneService timezoneService,
                                                            MessageService messageService) {
        return List.of(
                new GeFriendsCommand(keyboardService, friendshipService, messageService, localisationService),
                new GetFriendRequestsCommand(keyboardService, localisationService, friendshipService, messageService),
                new SendFriendRequestCommand(localisationService, friendshipService, messageService,
                        keyboardService, commandNavigator),
                new GoBackCommand(localisationService, commandNavigator),
                new ChangeTimezoneCommand(localisationService, messageService, tgUserService, timezoneService,
                        commandNavigator, keyboardService),
                new GetRemindersCommand(localisationService, messageService, keyboardService));
    }

    private List<CallbackBotCommand> getCallbackBotCommands(KeyboardService keyboardService, FriendshipService friendshipService,
                                                            LocalisationService localisationService, CommandNavigator commandNavigator,
                                                            ReminderService reminderService, TgUserService tgUserService, RequestParser requestParser,
                                                            ReminderMessageSender reminderMessageSender, MessageService messageService,
                                                            CallbackCommandNavigator callbackCommandNavigator,
                                                            SecurityService securityService) {
        return List.of(
                new CompleteCommand(reminderService, reminderMessageSender),
                new AcceptFriendRequestCommand(friendshipService, messageService),
                new RejectFriendRequestCommand(friendshipService, messageService),
                new DeleteFriendCommand(messageService, friendshipService),
                new CreateReminderCommand(localisationService, reminderService, messageService, keyboardService,
                        commandNavigator, requestParser, reminderMessageSender, tgUserService),
                new ChangeReminderTimeCommand(requestParser, reminderMessageSender,
                        messageService, reminderService, commandNavigator, keyboardService),
                new ChangeReminderTextCommand(reminderMessageSender, messageService, reminderService, commandNavigator, keyboardService),
                new PostponeReminderCommand(messageService, keyboardService, reminderService, requestParser, reminderMessageSender, commandNavigator),
                new DeleteReminderCommand(reminderService, reminderMessageSender),
                new CancelReminderCommand(reminderService, reminderMessageSender),
                new ReminderDetailsCommand(reminderService, reminderMessageSender, keyboardService, messageService, securityService),
                new CustomRemindCommand(messageService, keyboardService, requestParser, reminderService, reminderMessageSender, commandNavigator),
                new GetCompletedRemindersCommand(reminderService, reminderMessageSender),
                new GoBackCallbackCommand(callbackCommandNavigator),
                new GetActiveRemindersCommand(reminderService, reminderMessageSender),
                new DeleteCompletedReminderCommand(reminderService, reminderMessageSender),
                new ChangeReminderNoteCommand(reminderMessageSender, messageService, reminderService, commandNavigator, keyboardService),
                new DeleteReminderNoteCommand(reminderMessageSender, reminderService),
                new EditReminderCommand(reminderMessageSender, messageService, keyboardService, commandNavigator)
        );
    }

    private List<BotCommand> getBotCommands(KeyboardService keyboardService, ReminderService reminderService, TgUserService tgUserService, RequestParser requestParser, SecurityService securityService, ReminderMessageSender reminderMessageSender, MessageService messageService) {
        return List.of(
                new StartCommand(messageService, reminderService, tgUserService,
                        securityService, requestParser, keyboardService, reminderMessageSender),
                new HelpCommand(messageService));
    }
}
