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
import ru.gadjini.reminder.service.resolver.ReminderRequestParser;
import ru.gadjini.reminder.service.validation.ValidationService;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
public class BotConfiguration {

    @Bean
    public Collection<BotCommand> botCommands(KeyboardService keyboardService,
                                              MessageService messageService,
                                              ReminderService reminderService,
                                              TgUserService tgUserService,
                                              ReminderRequestParser reminderRequestParser,
                                              ValidationService validationService,
                                              SecurityService securityService,
                                              ReminderMessageSender reminderMessageSender) {
        return new ArrayList<>() {{
            add(new StartCommand(messageService, reminderService, tgUserService,
                    securityService, reminderRequestParser, keyboardService, validationService, reminderMessageSender));
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
                                                              ReminderRequestParser reminderRequestParser,
                                                              ValidationService validationService,
                                                              TgUserService tgUserService,
                                                              ReminderMessageSender reminderMessageSender) {
        return new ArrayList<>() {{
            add(new CompleteCommand(reminderService, reminderMessageSender));
            add(new AcceptFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new RejectFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new DeleteFriendCommand(messageService, friendshipService, localisationService));
            add(new CreateReminderCommand(localisationService, reminderService, messageService, keyboardService,
                    commandNavigator, reminderRequestParser, validationService, reminderMessageSender, tgUserService));
            add(new ChangeReminderTimeCommand(localisationService, reminderRequestParser, reminderMessageSender,
                    messageService, reminderService, commandNavigator));
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
