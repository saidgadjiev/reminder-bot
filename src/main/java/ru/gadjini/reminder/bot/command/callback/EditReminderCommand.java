package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;

public class EditReminderCommand implements CallbackBotCommand {

    private final String name = MessagesProperties.EDIT_REMINDER_COMMAND_NAME;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    public EditReminderCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        Reminder reminder = reminderService.getReminder(Integer.parseInt(arguments[0]));

        reminderMessageSender.sendReminderEditing(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), reminder);
    }
}
