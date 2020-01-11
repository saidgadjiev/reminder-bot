package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.RemindMessageService;

@Component
public class OkCommand implements CallbackBotCommand {

    private MessageService messageService;

    private RemindMessageService remindMessageService;

    @Autowired
    public OkCommand(MessageService messageService, RemindMessageService remindMessageService) {
        this.messageService = messageService;
        this.remindMessageService = remindMessageService;
    }

    @Override
    public String getName() {
        return CommandNames.OK_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());
        remindMessageService.delete(reminderId);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());

        return null;
    }
}
