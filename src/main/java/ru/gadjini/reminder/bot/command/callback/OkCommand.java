package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.message.MessageService;

@Component
public class OkCommand implements CallbackBotCommand {

    private MessageService messageService;

    @Autowired
    public OkCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return CommandNames.OK_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
        return null;
    }
}
