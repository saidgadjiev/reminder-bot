package ru.gadjini.reminder.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.gadjini.reminder.bot.command.HelpCommand;
import ru.gadjini.reminder.bot.command.StartCommand;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.callback.*;
import ru.gadjini.reminder.bot.command.keyboard.*;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.parser.RequestParser;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
public class BotConfiguration {

    @Bean
    public Collection<BotCommand> botCommands(KeyboardService keyboardService,
                                              MessageService messageService,
                                              ReminderService reminderService,
                                              TgUserService tgUserService,
                                              RequestParser requestParser,
                                              SecurityService securityService,
                                              ReminderMessageSender reminderMessageSender) {
        return new ArrayList<>() {{
            add(new StartCommand(messageService, reminderService, tgUserService,
                    securityService, requestParser, keyboardService, reminderMessageSender));
            add(new HelpCommand(messageService));
        }};
    }

    @Bean
    public Collection<CallbackBotCommand> callbackBotCommands(LocalisationService localisationService,
                                                              ReminderService reminderService,
                                                              FriendshipService friendshipService,
                                                              MessageService messageService,
                                                              KeyboardService keyboardService,
                                                              CommandNavigator commandNavigator,
                                                              RequestParser requestParser,
                                                              TgUserService tgUserService,
                                                              ReminderMessageSender reminderMessageSender) {
        return new ArrayList<>() {{
            add(new CompleteCommand(localisationService, reminderService, reminderMessageSender));
            add(new AcceptFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new RejectFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new DeleteFriendCommand(messageService, friendshipService, localisationService));
            add(new CreateReminderCommand(localisationService, reminderService, messageService, keyboardService,
                    commandNavigator, requestParser, reminderMessageSender, tgUserService));
            add(new ChangeReminderTimeCommand(localisationService, requestParser, reminderMessageSender,
                    messageService, reminderService, commandNavigator, keyboardService));
            add(new ChangeReminderTextCommand(reminderMessageSender, messageService, reminderService, commandNavigator, keyboardService));
            add(new PostponeReminderCommand(localisationService, messageService, keyboardService, reminderService, requestParser, reminderMessageSender, commandNavigator));
            add(new DeleteReminderCommand(localisationService, reminderService, reminderMessageSender));
            add(new CancelReminderCommand(localisationService, reminderService, reminderMessageSender));
            add(new CustomRemindCommand(messageService, keyboardService, requestParser, reminderService, reminderMessageSender, commandNavigator));
        }};
    }

    @Bean
    public Collection<KeyboardBotCommand> keyboardBotCommands(KeyboardService keyboardService,
                                                              FriendshipService friendshipService,
                                                              MessageService messageService,
                                                              ReminderService reminderService,
                                                              ReminderMessageSender reminderMessageSender,
                                                              LocalisationService localisationService,
                                                              CommandNavigator commandNavigator) {
        return new ArrayList<>() {{
            add(new GeFriendsCommand(keyboardService, friendshipService, messageService, localisationService));
            add(new GetFriendRequestsCommand(keyboardService, localisationService, friendshipService, messageService));
            add(new SendFriendRequestCommand(localisationService, friendshipService, messageService,
                    keyboardService, commandNavigator));
            add(new GoBackCommand(localisationService, commandNavigator));
            add(new GetReminders(localisationService, reminderService, reminderMessageSender));
        }};
    }
}
