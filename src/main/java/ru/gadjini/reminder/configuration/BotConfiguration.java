package ru.gadjini.reminder.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.gadjini.reminder.bot.command.HelpCommand;
import ru.gadjini.reminder.bot.command.StartCommand;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.callback.CompleteCommand;
import ru.gadjini.reminder.bot.command.keyboard.GeFriendsCommand;
import ru.gadjini.reminder.bot.command.keyboard.GetFriendRequestsCommand;
import ru.gadjini.reminder.service.*;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
public class BotConfiguration {

    @Bean
    public Collection<BotCommand> botCommands(KeyboardService keyboardService, MessageService messageService, ReminderService reminderService, TgUserService tgUserService, ReminderTextBuilder reminderTextBuilder) {
        return new ArrayList<>() {{
            add(new StartCommand(messageService, reminderService, tgUserService, reminderTextBuilder, keyboardService));
            add(new HelpCommand(messageService));
        }};
    }

    @Bean
    public Collection<CallbackBotCommand> callbackBotCommands(ReminderTextBuilder reminderTextBuilder, ReminderService reminderService, MessageService messageService) {
        return new ArrayList<>() {{
            add(new CompleteCommand(reminderTextBuilder, reminderService, messageService));
        }};
    }

    @Bean
    public Collection<KeyboardBotCommand> keyboardBotCommands(KeyboardService keyboardService, FriendshipService friendshipService, MessageService messageService, LocalisationService localisationService) {
        return new ArrayList<>() {{
            add(new GeFriendsCommand(keyboardService, friendshipService, messageService, localisationService));
            add(new GetFriendRequestsCommand(keyboardService, localisationService, friendshipService, messageService));
        }};
    }
}
