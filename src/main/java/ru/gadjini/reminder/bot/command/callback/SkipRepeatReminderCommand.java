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
import ru.gadjini.reminder.service.reminder.RepeatReminderService;

@Component
public class SkipRepeatReminderCommand implements CallbackBotCommand {

    private RepeatReminderService repeatReminderService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public SkipRepeatReminderCommand(RepeatReminderService repeatReminderService, ReminderMessageSender reminderMessageSender) {
        this.repeatReminderService = repeatReminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return MessagesProperties.SKIP_REPEAT_REMINDER_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = repeatReminderService.skip(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        reminderMessageSender.sendRepeatReminderSkipped(callbackQuery.getId(), reminder);
    }
}
