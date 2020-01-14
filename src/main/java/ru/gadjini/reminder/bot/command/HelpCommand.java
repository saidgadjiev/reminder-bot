package ru.gadjini.reminder.bot.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class HelpCommand extends BotCommand {

    private final MessageService messageService;

    private LocalisationService localisationService;

    @Autowired
    public HelpCommand(MessageService messageService, LocalisationService localisationService) {
        super(CommandNames.HELP_COMMAND_NAME, "");
        this.messageService = messageService;
        this.localisationService = localisationService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(chat.getId()).text(localisationService.getMessage(MessagesProperties.MESSAGE_HELP)));
    }
}
