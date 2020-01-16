package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class DeleteReminderCommand implements CallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public DeleteReminderCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.name = CommandNames.DELETE_REMINDER_COMMAND_NAME;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());

        Reminder reminder = reminderService.delete(reminderId);
        if (reminder == null) {
            reminderMessageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());

            return MessagesProperties.MESSAGE_REMINDER_NOT_FOUND;
        } else {
            reminderMessageSender.sendReminderDeleted(callbackQuery.getMessage().getMessageId(), reminder);

            return MessagesProperties.MESSAGE_REMINDER_DELETED_ANSWER;
        }
    }
}
