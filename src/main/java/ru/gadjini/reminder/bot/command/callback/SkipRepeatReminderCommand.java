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
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService;
import ru.gadjini.reminder.util.KeyboardCustomizer;

@Component
public class SkipRepeatReminderCommand implements CallbackBotCommand {

    private RepeatReminderBusinessService reminderBusinessService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public SkipRepeatReminderCommand(RepeatReminderBusinessService reminderBusinessService, ReminderMessageSender reminderMessageSender) {
        this.reminderBusinessService = reminderBusinessService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public String getName() {
        return CommandNames.SKIP_REPEAT_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderBusinessService.skip(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        boolean isCalledFromReminderDetails = new KeyboardCustomizer(callbackQuery.getMessage().getReplyMarkup()).hasButton(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME);
        if (isCalledFromReminderDetails) {
            reminderMessageSender.sendRepeatReminderSkippedFromList(callbackQuery.getMessage().getMessageId(), callbackQuery.getMessage().getReplyMarkup(), reminder);
        } else {
            reminderMessageSender.sendRepeatReminderSkipped(reminder);
        }

        return MessagesProperties.MESSAGE_REMINDER_SKIPPED_ANSWER;
    }
}
