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
import ru.gadjini.reminder.service.reminder.simple.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;

@Component
public class ReadReminderCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private ReminderMessageSender messageSender;

    @Autowired
    public ReadReminderCommand(ReminderService reminderService, ReminderMessageSender messageSender) {
        this.reminderService = reminderService;
        this.messageSender = messageSender;
    }

    @Override
    public String getName() {
        return CommandNames.READ_REMINDER_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());
        Reminder reminder = reminderService.read(reminderId);

        messageSender.sendReminderRead(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getMessage().getReplyMarkup(),
                reminder
        );

        return MessagesProperties.MESSAGE_READ_REMINDER_ANSWER;
    }
}
