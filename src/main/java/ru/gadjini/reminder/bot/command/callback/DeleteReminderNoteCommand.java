package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;

public class DeleteReminderNoteCommand implements CallbackBotCommand {

    private String name;

    private ReminderMessageSender reminderMessageSender;

    private ReminderService reminderService;

    public DeleteReminderNoteCommand(ReminderMessageSender reminderMessageSender, ReminderService reminderService) {
        this.reminderMessageSender = reminderMessageSender;
        this.reminderService = reminderService;
        name = MessagesProperties.DELETE_REMINDER_NOTE_COMMAND_NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        Reminder reminder = reminderService.deleteReminderNote(Integer.parseInt(arguments[0]));
        reminder.getCreator().setChatId(callbackQuery.getMessage().getChatId());

        reminderMessageSender.sendReminderNoteDeleted(callbackQuery.getId(), callbackQuery.getMessage().getMessageId(), reminder);
    }
}