package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.LocalisationService;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;

import java.util.List;

public class GetReminders implements KeyboardBotCommand {

    private String name;

    private ReminderService reminderService;

    private ReminderMessageSender reminderMessageSender;

    public GetReminders(LocalisationService localisationService, ReminderService reminderService, ReminderMessageSender reminderMessageSender) {
        this.name = localisationService.getMessage(MessagesProperties.COMMAND_GET_REMINDERS_NAME);
        this.reminderService = reminderService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        List<Reminder> reminders = reminderService.getReminders();

        reminderMessageSender.sendReminders(message.getChatId(), reminders);
    }
}
