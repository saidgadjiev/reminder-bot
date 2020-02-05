package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.InviteService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class CreateInviteCommand implements KeyboardBotCommand {

    private Set<String> names = new HashSet<>();

    private InviteService inviteService;

    private MessageService messageService;

    public CreateInviteCommand(LocalisationService localisationService, InviteService inviteService, MessageService messageService) {
        this.inviteService = inviteService;
        this.messageService = messageService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.CREATE_INVITE_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        if (message.getFrom().getId() == 171271164) {
            String token = inviteService.createInvite();

            messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(message.getChatId()).text(token));
        }

        return false;
    }
}
