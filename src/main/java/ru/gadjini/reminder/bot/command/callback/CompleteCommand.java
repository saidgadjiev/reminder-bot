package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.ReminderTextBuilder;
import ru.gadjini.reminder.util.UserUtils;

public class CompleteCommand implements CallbackBotCommand {

    private ReminderTextBuilder reminderTextBuilder;

    private ReminderService reminderService;

    private MessageService messageService;

    public CompleteCommand(ReminderTextBuilder reminderTextBuilder, ReminderService reminderService, MessageService messageService) {
        this.reminderTextBuilder = reminderTextBuilder;
        this.reminderService = reminderService;
        this.messageService = messageService;
    }

    @Override
    public String getName() {
        return MessagesProperties.COMPLETE_REMINDER_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, String[] arguments) {
        int reminderId = Integer.parseInt(arguments[0]);
        Reminder reminder = reminderService.deleteReminder(reminderId);
        String reminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAt());

        sendMessages(callbackQuery.getId(), reminderText, reminder);
    }

    private void sendMessages(String queryId, String reminderText, Reminder reminder) {
        messageService.sendMessageByCode(
                reminder.getCreator().getChatId(),
                MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                new Object[]{UserUtils.userLink(reminder.getReceiver()), reminderText}
        );
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
        messageService.sendMessageByCode(
                reminder.getReceiver().getChatId(),
                MessagesProperties.MESSAGE_REMINDER_COMPLETED_FROM,
                new Object[]{reminderText, UserUtils.userLink(reminder.getCreator())}
        );
    }
}
