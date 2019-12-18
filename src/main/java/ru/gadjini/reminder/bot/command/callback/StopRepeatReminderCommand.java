package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

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
        return MessagesProperties.STOP_REPEAT_REMINDER_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.delete(requestParams.getInt(Arg.REMINDER_ID.getKey()));
        reminderMessageSender.sendRepeatReminderStopped(callbackQuery.getId(), reminder);
    }
}
