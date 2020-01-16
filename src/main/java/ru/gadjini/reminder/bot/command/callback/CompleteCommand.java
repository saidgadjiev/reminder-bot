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
        this.name = CommandNames.COMPLETE_REMINDER_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());

        Reminder reminder = reminderService.completeReminder(reminderId);
        String currHistoryName = requestParams.getString(Arg.CURR_HISTORY_NAME.getKey());

        if (Objects.equals(currHistoryName, CommandNames.REMINDER_DETAILS_COMMAND_NAME)) {
            return doCompleteFromList(reminder, callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
        } else {
            return doComplete(reminder, callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
        }
    }

    private String doCompleteFromList(Reminder reminder, long chatId, int messageId) {
        if (reminder == null) {
            reminderMessageSender.sendReminderCantBeCompletedFromList(chatId, messageId);

            return MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED;
        } else {
            reminderMessageSender.sendReminderCompletedFromList(messageId, reminder);

            return MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER;
        }
    }

    private String doComplete(Reminder reminder, long chatId, int messageId) {
        if (reminder == null) {
            reminderMessageSender.sendReminderCantBeCompleted(chatId, messageId);

            return MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED;
        } else {
            reminderMessageSender.sendReminderCompleted(reminder);

            return MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER;
        }
    }

}

