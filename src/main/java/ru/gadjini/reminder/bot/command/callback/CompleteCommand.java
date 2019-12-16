package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

import java.util.Objects;

@Component
public class CompleteCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    private String name;

    @Autowired
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
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());

        Reminder reminder = reminderService.completeReminder(reminderId);
        String currHistoryName = requestParams.getString(Arg.CURR_HISTORY_NAME.getKey());

        if (Objects.equals(currHistoryName, MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)) {
            if (reminder == null) {
                reminderMessageSender.sendReminderCantBeCompletedFromList(
                        callbackQuery.getMessage().getChatId(), callbackQuery.getId(),
                        callbackQuery.getMessage().getMessageId());
            } else {
                reminder.getReceiver().setChatId(callbackQuery.getMessage().getChatId());

                reminderMessageSender.sendReminderCompletedFromList(callbackQuery.getId(), callbackQuery.getMessage().getMessageId(), reminder);
            }
        } else {
            if (reminder == null) {
                reminderMessageSender.sendReminderCantBeCompleted(callbackQuery.getMessage().getChatId(), callbackQuery.getId(), callbackQuery.getMessage().getMessageId());
            } else {
                reminder.getReceiver().setChatId(callbackQuery.getMessage().getChatId());

                reminderMessageSender.sendReminderCompleted(callbackQuery.getId(), reminder);
            }
        }
    }
}
