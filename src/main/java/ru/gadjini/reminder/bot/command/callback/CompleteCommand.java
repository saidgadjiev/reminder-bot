package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderService;

public class CompleteCommand implements CallbackBotCommand {

    private ReminderService reminderService;

    private MessageService messageService;

    public CompleteCommand(ReminderService reminderService, MessageService messageService) {
        this.reminderService = reminderService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return MessagesProperties.COMPLETE_REMINDER_COMMAND_NAME;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        int reminderId = Integer.parseInt(arguments[0]);

        reminderService.deleteReminder(reminderId);
    }
}
