package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.InviteService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class CreateInviteCommand implements KeyboardBotCommand {

    private String name;

    private InviteService inviteService;

    private MessageService messageService;

    public CreateInviteCommand(LocalisationService localisationService, InviteService inviteService, MessageService messageService) {
        this.name = localisationService.getMessage(MessagesProperties.CREATE_INVITE_COMMAND_NAME);
        this.inviteService = inviteService;
        this.messageService = messageService;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        if (message.getFrom().getId() == 171271164) {
            String token = inviteService.createInvite();

            messageService.sendMessage(new SendMessageContext().chatId(message.getChatId()).text(token));
        }

        return false;
    }
}
