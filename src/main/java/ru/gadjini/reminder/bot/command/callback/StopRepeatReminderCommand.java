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
public class StopRepeatReminderCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public StopRepeatReminderCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return CommandNames.STOP_REPEAT_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.delete(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        String currHistoryName = requestParams.getString(Arg.CURR_HISTORY_NAME.getKey());
        if (Objects.equals(currHistoryName, CommandNames.REMINDER_DETAILS_COMMAND_NAME)) {
            reminderMessageSender.sendRepeatReminderStoppedFromList(callbackQuery.getMessage().getMessageId(), reminder);

            return MessagesProperties.MESSAGE_REMINDER_STOPPED_ANSWER;
        } else {
            reminderMessageSender.sendRepeatReminderStopped(reminder);

            return MessagesProperties.MESSAGE_REMINDER_STOPPED_ANSWER;
        }
    }
}
