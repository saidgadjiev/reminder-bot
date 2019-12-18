package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;

import java.util.List;

@Component
public class GetCompletedRemindersCommand implements CallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public GetCompletedRemindersCommand(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.name = MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<Reminder> reminders = reminderService.getCompletedReminders();

        reminderMessageSender.sendCompletedReminders(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                reminders
        );
    }
}
