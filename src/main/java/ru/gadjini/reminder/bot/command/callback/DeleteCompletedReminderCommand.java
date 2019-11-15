package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;

public class DeleteCompletedReminderCommand implements CallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

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
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        reminderService.deleteMyCompletedReminders();

        reminderMessageSender.sendCompletedRemindersDeleted(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
