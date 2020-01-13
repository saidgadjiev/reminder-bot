package ru.gadjini.reminder.bot.command.keyboard;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class RefreshUserDataCommand implements KeyboardBotCommand {

    private String name;

    private TgUserService tgUserService;

    private LocalisationService localisationService;

    private MessageService messageService;

    @Autowired
    public RefreshUserDataCommand(LocalisationService localisationService, TgUserService tgUserService, LocalisationService localisationService1, MessageService messageService) {
        this.name = localisationService.getMessage(MessagesProperties.REFRESH_USER_DATA_COMMAND_NAME);
        this.tgUserService = tgUserService;
        this.localisationService = localisationService1;
        this.messageService = messageService;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        TgUser user = tgUserService.createOrUpdateUser(message.getChatId(), message.getFrom());
        messageService.sendMessage(new SendMessageContext().chatId(message.getChatId()).text(message(user)));

        return false;
    }

    private String message(TgUser user) {
        StringBuilder message = new StringBuilder();

        message.append(localisationService.getMessage(MessagesProperties.MESSAGE_USER_DATA_REFRESHED)).append("\n");
        message.append(localisationService.getMessage(MessagesProperties.MESSAGE_USER_DATA, new Object[]{
                user.getName(),
                StringUtils.isNotBlank(user.getUsername()) ? TgUser.USERNAME_START + user.getUsername() : localisationService.getMessage(MessagesProperties.MESSAGE_USERNAME_NOT_EXISTS),
                user.getUserId()
        }));

        return message.toString();
    }
}
