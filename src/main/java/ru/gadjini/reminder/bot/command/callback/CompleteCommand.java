package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

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
            doCompleteFromList(reminder, callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), callbackQuery.getId());
        } else {
            doComplete(reminder, callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), callbackQuery.getId());
        }
    }

    private void doCompleteFromList(Reminder reminder, long chatId, int messageId, String queryId) {
        if (reminder == null) {
            reminderMessageSender.sendReminderCantBeCompletedFromList(chatId, queryId, messageId);
        } else {
            reminder.getReceiver().setChatId(chatId);
            reminderMessageSender.sendReminderCompletedFromList(queryId, messageId, reminder);
        }
    }

    private void doComplete(Reminder reminder, long chatId, int messageId, String queryId) {
        if (reminder == null) {
            reminderMessageSender.sendReminderCantBeCompleted(chatId, queryId, messageId);
        } else {
            reminder.getReceiver().setChatId(chatId);
            reminderMessageSender.sendReminderCompleted(queryId, reminder);
        }
    }

}

