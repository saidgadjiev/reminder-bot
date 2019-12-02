package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

public class CompleteCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private String name;

    public CompleteCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.name = MessagesProperties.COMPLETE_REMINDER_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        int reminderId = Integer.parseInt(arguments[0]);

        Reminder reminder = reminderService.completeReminder(reminderId);
        String currHistoryName = arguments[1];

        if (currHistoryName.equals(MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)) {
            if (reminder == null) {
                reminderMessageSender.sendReminderCantBeCompletedFromList(
                        callbackQuery.getMessage().getChatId(), callbackQuery.getId(),
                        callbackQuery.getMessage().getMessageId());
            } else {
                reminder.getReceiver().setChatId(callbackQuery.getMessage().getChatId());

                reminderMessageSender.sendReminderCompleteFromList(callbackQuery.getId(), callbackQuery.getMessage().getMessageId(), reminder);
            }
        } else {
            if (reminder == null) {
                reminderMessageSender.sendReminderCantBeCompleted(callbackQuery.getMessage().getChatId(), callbackQuery.getId(), callbackQuery.getMessage().getMessageId());
            } else {
                reminder.getReceiver().setChatId(callbackQuery.getMessage().getChatId());

                reminderMessageSender.sendReminderComplete(callbackQuery.getId(), reminder);
            }
        }
    }
}
