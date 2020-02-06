package ru.gadjini.reminder.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class HelpCommand extends BotCommand implements KeyboardBotCommand {

    private final MessageService messageService;

    private LocalisationService localisationService;

    private Set<String> names = new HashSet<>();

    @Autowired
    public HelpCommand(MessageService messageService, LocalisationService localisationService) {
        super(CommandNames.HELP_COMMAND_NAME, "");
        this.messageService = messageService;
        this.localisationService = localisationService;
        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.HELP_COMMAND_NAME, locale));
        }
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        sendHelpMessage(user.getId(), localisationService.getCurrentLocale(user.getLanguageCode()));
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        sendHelpMessage(message.getFrom().getId(), localisationService.getCurrentLocale(message.getFrom().getLanguageCode()));

        return false;
    }

    private void sendHelpMessage(int userId, Locale locale) {
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(userId)
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_HELP, locale)));
    }
}
