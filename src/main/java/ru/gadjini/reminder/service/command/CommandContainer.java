package ru.gadjini.reminder.service.command;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.gadjini.reminder.bot.command.HelpCommand;
import ru.gadjini.reminder.bot.command.StartCommand;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.callback.*;
import ru.gadjini.reminder.bot.command.keyboard.*;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.TimezoneService;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.MessageBuilder;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.service.security.SecurityService;

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
                            SecurityService securityService,
                            ReminderMessageSender reminderMessageSender,
                            MessageService messageService,
                            ReminderRequestService reminderRequestService,
                            CallbackCommandNavigator callbackCommandNavigator,
                            MessageBuilder messageBuilder,
                            TimeBuilder timeBuilder,
                            UserReminderNotificationService userReminderNotificationService,
                            ReminderNotificationService reminderNotificationService,
                            TimezoneService timezoneService) {
        for (BotCommand botCommand : getBotCommands(keyboardService, reminderRequestService, tgUserService, reminderMessageSender, messageService)) {
            botCommandRegistryMap.put(botCommand.getCommandIdentifier(), botCommand);
        }
        for (CallbackBotCommand botCommand : getCallbackBotCommands(
                keyboardService, friendshipService,
                commandNavigator, reminderService, reminderRequestService,
                reminderMessageSender, messageService, callbackCommandNavigator,
                localisationService, messageBuilder, timeBuilder, tgUserService,
                reminderNotificationService, userReminderNotificationService, securityService
        )) {
            callbackBotCommandMap.put(botCommand.getName(), botCommand);
        }
        keyboardBotCommands = getKeyboardBotCommands(
                keyboardService,
                friendshipService,
                localisationService,
                commandNavigator,
                tgUserService,
                timezoneService,
                userReminderNotificationService,
                messageBuilder,
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
                                                            UserReminderNotificationService userReminderNotificationService,
                                                            MessageBuilder messageBuilder,
                                                            MessageService messageService) {
        return List.of(
                new GeFriendsCommand(keyboardService, friendshipService, messageService, localisationService),
                new GetFriendRequestsCommand(keyboardService, localisationService, friendshipService, messageService),
                new SendFriendRequestCommand(localisationService, friendshipService, messageService,
                        keyboardService, commandNavigator),
                new GoBackCommand(localisationService, commandNavigator),
                new ChangeTimezoneCommand(localisationService, messageService, tgUserService, timezoneService,
                        commandNavigator, keyboardService),
                new GetRemindersCommand(localisationService, messageService, keyboardService),
                new UserReminderNotificationScheduleCommand(
                        UserReminderNotification.NotificationType.WITH_TIME,
                        localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITH_TIME_COMMAND_NAME),
                        MessagesProperties.USER_REMINDER_NOTIFICATION_WITH_TIME_HISTORY_NAME,
                        userReminderNotificationService,
                        messageBuilder,
                        messageService,
                        keyboardService
                ),
                new UserReminderNotificationScheduleCommand(
                        UserReminderNotification.NotificationType.WITHOUT_TIME,
                        localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_COMMAND_NAME),
                        MessagesProperties.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_HISTORY_NAME,
                        userReminderNotificationService,
                        messageBuilder,
                        messageService,
                        keyboardService
                ),
                new UserReminderNotificationCommand(messageService, localisationService, keyboardService)
        );
    }

    private List<CallbackBotCommand> getCallbackBotCommands(KeyboardService keyboardService,
                                                            FriendshipService friendshipService,
                                                            CommandNavigator commandNavigator,
                                                            ReminderService reminderService,
                                                            ReminderRequestService reminderRequestService,
                                                            ReminderMessageSender reminderMessageSender,
                                                            MessageService messageService,
                                                            CallbackCommandNavigator callbackCommandNavigator,
                                                            LocalisationService localisationService,
                                                            MessageBuilder messageBuilder,
                                                            TimeBuilder timeBuilder,
                                                            TgUserService userService,
                                                            ReminderNotificationService reminderNotificationService,
                                                            UserReminderNotificationService userReminderNotificationService,
                                                            SecurityService securityService) {
        return List.of(
                new CompleteCommand(reminderService, reminderMessageSender),
                new AcceptFriendRequestCommand(friendshipService, messageService),
                new RejectFriendRequestCommand(friendshipService, messageService),
                new DeleteFriendCommand(messageService, friendshipService, keyboardService),
                new CreateReminderCommand(reminderRequestService, messageService, keyboardService, commandNavigator, reminderMessageSender),
                new ChangeReminderTimeCommand(reminderMessageSender, messageService, reminderRequestService, commandNavigator, keyboardService, localisationService),
                new ChangeReminderTextCommand(reminderMessageSender, messageService, reminderService, commandNavigator, keyboardService, localisationService),
                new PostponeReminderCommand(messageService, keyboardService, reminderRequestService, reminderMessageSender, commandNavigator, localisationService),
                new DeleteReminderCommand(reminderService, reminderMessageSender),
                new CancelReminderCommand(reminderService, reminderMessageSender),
                new ReminderDetailsCommand(reminderService, reminderMessageSender, keyboardService, messageService, securityService, messageBuilder),
                new CustomRemindCommand(messageService, keyboardService, reminderRequestService, reminderMessageSender, commandNavigator, localisationService),
                new GetCompletedRemindersCommand(reminderService, reminderMessageSender),
                new GoBackCallbackCommand(callbackCommandNavigator, commandNavigator),
                new GetActiveRemindersCommand(reminderService, reminderMessageSender),
                new DeleteCompletedReminderCommand(reminderService, reminderMessageSender),
                new ChangeReminderNoteCommand(reminderMessageSender, messageService, reminderService, commandNavigator, keyboardService, localisationService),
                new DeleteReminderNoteCommand(reminderMessageSender, reminderService),
                new EditReminderCommand(reminderMessageSender, messageService, keyboardService, messageBuilder, reminderService),
                new ReceiverReminderCommand(reminderMessageSender, messageService, keyboardService, messageBuilder, reminderService, commandNavigator),
                new FriendDetailsCommand(userService, messageService, keyboardService, localisationService),
                new OkCommand(messageService),
                new ReminderTimeScheduleCommand(reminderNotificationService, messageService, keyboardService, timeBuilder),
                new ReminderTimeDetailsCommand(reminderNotificationService, messageBuilder, messageService, keyboardService),
                new DeleteReminderTimeCommand(reminderNotificationService, messageService, keyboardService),
                new DeleteUserReminderNotificationCommand(messageBuilder, keyboardService, messageService, userReminderNotificationService)
        );
    }

    private List<BotCommand> getBotCommands(KeyboardService keyboardService,
                                            ReminderRequestService reminderRequestService,
                                            TgUserService tgUserService,
                                            ReminderMessageSender reminderMessageSender,
                                            MessageService messageService) {
        return List.of(
                new StartCommand(messageService, reminderRequestService, tgUserService, keyboardService, reminderMessageSender),
                new HelpCommand(messageService));
    }
}
