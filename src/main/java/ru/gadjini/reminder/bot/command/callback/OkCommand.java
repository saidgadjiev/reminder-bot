package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;

@Component
public class OkCommand implements CallbackBotCommand {

    private MessageService messageService;

    private ReminderService reminderService;

    @Autowired
    public OkCommand(MessageService messageService, ReminderService reminderService) {
        this.messageService = messageService;
        this.reminderService = reminderService;
    }

    @Override
    public String getName() {
        return CommandNames.OK_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());
        reminderService.deleteReceiverMessage(reminderId);
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());

        return null;
    }
}
