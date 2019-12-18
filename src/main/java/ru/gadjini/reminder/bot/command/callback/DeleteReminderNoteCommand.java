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
public class DeleteReminderNoteCommand implements CallbackBotCommand {

    private String name;

    private ReminderMessageSender reminderMessageSender;

    private ReminderService reminderService;

    @Autowired
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
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        Reminder reminder = reminderService.deleteReminderNote(requestParams.getInt(Arg.REMINDER_ID.getKey()));
        reminder.getCreator().setChatId(callbackQuery.getMessage().getChatId());

        reminderMessageSender.sendReminderNoteDeleted(callbackQuery.getId(), callbackQuery.getMessage().getMessageId(), reminder);
    }
}
