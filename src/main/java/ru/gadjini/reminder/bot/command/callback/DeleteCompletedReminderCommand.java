package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

@Component
public class DeleteCompletedReminderCommand implements CallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public DeleteCompletedReminderCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.name = MessagesProperties.DELETE_COMPLETED_REMINDERS_COMMAND_NAME;
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        reminderService.deleteMyCompletedReminders();

        reminderMessageSender.sendCompletedRemindersDeleted(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
