package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.CommandNavigator;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;

import java.util.List;

public class GetCompletedReminders implements CallbackBotCommand, NavigableCallbackBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    public GetCompletedReminders(ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
        this.name = MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        List<Reminder> reminders = reminderService.getCompletedReminders();

        reminderMessageSender.sendCompletedReminders(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                callbackQuery.getId(),
                reminders
        );
    }


    @Override
    public String getHistoryName() {
        return MessagesProperties.GET_COMPLETED_REMINDERS_COMMAND_NAME;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId) {
        List<Reminder> reminders = reminderService.getCompletedReminders();

        reminderMessageSender.sendCompletedReminders(chatId, messageId, queryId, reminders);
    }
}
