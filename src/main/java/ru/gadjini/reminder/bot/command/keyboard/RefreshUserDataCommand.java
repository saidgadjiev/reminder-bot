package ru.gadjini.reminder.bot.command.keyboard;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.UserUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class RefreshUserDataCommand implements KeyboardBotCommand {

    private Set<String> names = new HashSet<>();

    private TgUserService tgUserService;

    private LocalisationService localisationService;

    private MessageService messageService;

    @Autowired
    public RefreshUserDataCommand(LocalisationService localisationService, TgUserService tgUserService, LocalisationService localisationService1, MessageService messageService) {
        this.tgUserService = tgUserService;
        this.localisationService = localisationService1;
        this.messageService = messageService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.REFRESH_USER_DATA_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        TgUser user = tgUserService.createOrUpdateUser(message.getChatId(), message.getFrom());
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(message.getChatId()).text(message(user)));

        return false;
    }

    private String message(TgUser user) {
        StringBuilder message = new StringBuilder();

        message.append(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_DATA_REFRESHED)).append("\n");
        message.append(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_DATA, new Object[]{
                user.getName(),
                StringUtils.isNotBlank(user.getUsername()) ? TgUser.USERNAME_START + user.getUsername() : localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USERNAME_NOT_EXISTS),
                UserUtils.userLink(user.getUserId())
        }));

        return message.toString();
    }
}
