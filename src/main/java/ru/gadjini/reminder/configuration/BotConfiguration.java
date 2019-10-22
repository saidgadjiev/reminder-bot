package ru.gadjini.reminder.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.gadjini.reminder.bot.command.HelpCommand;
import ru.gadjini.reminder.bot.command.StartCommand;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.callback.*;
import ru.gadjini.reminder.bot.command.keyboard.GeFriendsCommand;
import ru.gadjini.reminder.bot.command.keyboard.GetFriendRequestsCommand;
import ru.gadjini.reminder.bot.command.keyboard.GoBackCommand;
import ru.gadjini.reminder.bot.command.keyboard.SendFriendRequestCommand;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.resolver.ReminderRequestResolver;
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
                                              ReminderTextBuilder reminderTextBuilder,
                                              ReminderRequestResolver reminderRequestResolver,
                                              ValidationService validationService) {
        return new ArrayList<>() {{
            add(new StartCommand(messageService, reminderService, tgUserService, reminderTextBuilder,
                    reminderRequestResolver, keyboardService, validationService));
            add(new HelpCommand(messageService));
        }};
    }

    @Bean
    public Collection<CallbackBotCommand> callbackBotCommands(LocalisationService localisationService,
                                                              ReminderTextBuilder reminderTextBuilder,
                                                              ReminderService reminderService,
                                                              FriendshipService friendshipService,
                                                              MessageService messageService,
                                                              KeyboardService keyboardService,
                                                              CommandNavigator commandNavigator,
                                                              ReminderMessageSender reminderMessageSender,
                                                              ReminderRequestResolver reminderRequestResolver,
                                                              ValidationService validationService) {
        return new ArrayList<>() {{
            add(new CompleteCommand(reminderService, reminderMessageSender));
            add(new AcceptFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new RejectFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new DeleteFriendCommand(messageService, friendshipService, localisationService));
            add(new CreateReminderCommand(localisationService, reminderService, messageService, reminderTextBuilder,
                    keyboardService, commandNavigator, reminderRequestResolver, validationService));
        }};
    }

    @Bean
    public Collection<KeyboardBotCommand> keyboardBotCommands(KeyboardService keyboardService,
                                                              FriendshipService friendshipService,
                                                              MessageService messageService,
                                                              LocalisationService localisationService,
                                                              CommandNavigator commandNavigator) {
        return new ArrayList<>() {{
            add(new GeFriendsCommand(keyboardService, friendshipService, messageService, localisationService));
            add(new GetFriendRequestsCommand(keyboardService, localisationService, friendshipService, messageService));
            add(new SendFriendRequestCommand(localisationService, friendshipService, messageService,
                    keyboardService, commandNavigator));
            add(new GoBackCommand(localisationService, commandNavigator));
        }};
    }
}
