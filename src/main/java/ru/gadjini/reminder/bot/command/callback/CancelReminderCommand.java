package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

public class CancelReminderCommand implements CallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    public CancelReminderCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.name = MessagesProperties.CANCEL_REMINDER_COMMAND_NAME;
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
        String currHistoryName = arguments[1];

        if (currHistoryName.equals(MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)) {
            if (reminder == null) {
                reminderMessageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getId(), callbackQuery.getMessage().getMessageId());
            } else {
                reminder.getReceiver().setChatId(callbackQuery.getMessage().getChatId());
                reminderMessageSender.sendReminderCanceledFromList(callbackQuery.getId(), callbackQuery.getMessage().getMessageId(), reminder);
            }
        } else {
            if (reminder == null) {
                reminderMessageSender.sendReminderNotFound(callbackQuery.getMessage().getChatId(), callbackQuery.getId(), callbackQuery.getMessage().getMessageId());
            } else {
                reminder.getReceiver().setChatId(callbackQuery.getMessage().getChatId());
                reminderMessageSender.sendReminderCanceled(callbackQuery.getId(), callbackQuery.getMessage().getMessageId(), reminder);
            }
        }
    }
}
