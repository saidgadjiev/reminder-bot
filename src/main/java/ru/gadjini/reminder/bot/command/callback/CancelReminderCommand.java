package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;

public class CancelReminderCommand implements CallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    public CancelReminderCommand(LocalisationService localisationService, ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.name = localisationService.getMessage(MessagesProperties.CANCEL_REMINDER_COMMAND_NAME);
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        int reminderId = Integer.parseInt(arguments[0]);

        Reminder reminder = reminderService.cancel(reminderId);
        if (reminder == null) {
            reminderMessageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getId(), callbackQuery.getMessage().getMessageId());
        } else {
            reminder.getReceiver().setChatId(callbackQuery.getMessage().getChatId());
            reminderMessageSender.sendReminderCanceled(callbackQuery.getId(), callbackQuery.getMessage().getMessageId(), reminder);
        }
    }
}
