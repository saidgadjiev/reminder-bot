package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.ReminderTextBuilder;

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
    public void processMessage(AbsSender absSender, CallbackQuery callbackQuery, String[] arguments) {
        int reminderId = Integer.parseInt(arguments[0]);
        Reminder reminder = reminderService.deleteReminder(reminderId);
        String reminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAt());

        messageService.sendMessageByCode(
                reminder.getCreator().getChatId(),
                MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                new Object[]{TgUser.USERNAME_START + reminder.getReceiver().getUsername(), reminderText}
        );

        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
        messageService.sendMessageByCode(
                reminder.getCreator().getChatId(),
                MessagesProperties.MESSAGE_REMINDER_COMPLETED_FROM,
                new Object[]{reminderText, TgUser.USERNAME_START + reminder.getCreator().getUsername()}
        );
    }
}
