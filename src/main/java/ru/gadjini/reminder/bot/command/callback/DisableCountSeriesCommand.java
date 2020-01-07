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
import ru.gadjini.reminder.service.reminder.RepeatReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class DisableCountSeriesCommand implements CallbackBotCommand {

    private RepeatReminderService reminderService;

    private ReminderMessageSender messageSender;

    @Autowired
    public DisableCountSeriesCommand(RepeatReminderService reminderService, ReminderMessageSender messageSender) {
        this.reminderService = reminderService;
        this.messageSender = messageSender;
    }

    @Override
    public String getName() {
        return CommandNames.DISABLE_COUNT_SERIES_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.disableCountSeries(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageSender.sendCountSeriesEnabledOrDisabled(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), callbackQuery.getFrom().getId(), reminder);

        return MessagesProperties.MESSAGE_COUNT_SERIES_DISABLED_ANSWER;
    }
}
