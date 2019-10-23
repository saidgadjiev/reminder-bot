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
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.*;
import ru.gadjini.reminder.service.resolver.ReminderRequestResolver;
import ru.gadjini.reminder.service.resolver.matcher.LoginTextTimeMatcher;
import ru.gadjini.reminder.service.resolver.matcher.RequestMatcher;
import ru.gadjini.reminder.service.resolver.matcher.TextTimeRequestMatcher;
import ru.gadjini.reminder.service.validation.ValidationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Configuration
public class BotConfiguration {

    @Bean
    public Collection<BotCommand> botCommands(KeyboardService keyboardService,
                                              MessageService messageService,
                                              ReminderService reminderService,
                                              TgUserService tgUserService,
                                              ReminderRequestResolver reminderRequestResolver,
                                              ValidationService validationService,
                                              ReminderMessageSender reminderMessageSender) {
        return new ArrayList<>() {{
            add(new StartCommand(messageService, reminderService, tgUserService,
                    reminderRequestResolver, keyboardService, validationService, reminderMessageSender));
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
                                                              ReminderRequestResolver reminderRequestResolver,
                                                              ValidationService validationService,
                                                              ReminderMessageSender reminderMessageSender) {
        return new ArrayList<>() {{
            add(new CompleteCommand(reminderService, reminderMessageSender));
            add(new AcceptFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new RejectFriendRequestCommand(localisationService, friendshipService, messageService));
            add(new DeleteFriendCommand(messageService, friendshipService, localisationService));
            add(new CreateReminderCommand(localisationService, reminderService, messageService,
                    keyboardService, commandNavigator, reminderRequestResolver, validationService, reminderMessageSender));
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

    @Bean
    public List<RequestMatcher> requestParsers(DateService dateService) {
        return new ArrayList<>() {{
            add(new LoginTextTimeMatcher(dateService));
            add(new TextTimeRequestMatcher(dateService));
        }};
    }
}
