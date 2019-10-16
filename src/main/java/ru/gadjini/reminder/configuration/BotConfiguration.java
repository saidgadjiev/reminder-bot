package ru.gadjini.reminder.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.gadjini.reminder.bot.command.HelpCommand;
import ru.gadjini.reminder.bot.command.StartCommand;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.callback.CompleteCommand;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.ReminderTextBuilder;
import ru.gadjini.reminder.service.TgUserService;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
public class BotConfiguration {

    @Bean
    public Collection<BotCommand> botCommands(MessageService messageService, ReminderService reminderService, TgUserService tgUserService, ReminderTextBuilder reminderTextBuilder) {
        return new ArrayList<>() {{
            add(new StartCommand(messageService, reminderService, tgUserService, reminderTextBuilder));
            add(new HelpCommand(messageService));
        }};
    }

    @Bean
    public Collection<CallbackBotCommand> callbackBotCommands(ReminderTextBuilder reminderTextBuilder, ReminderService reminderService, MessageService messageService) {
        return new ArrayList<>() {{
            add(new CompleteCommand(reminderTextBuilder, reminderService, messageService));
        }};
    }
}
