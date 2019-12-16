package ru.gadjini.reminder.bot.command.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.MessageBuilder;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;

public class ReminderTimeDetailsCommand implements CallbackBotCommand {

    private ReminderNotificationService reminderNotificationService;

    private MessageBuilder messageBuilder;

    private MessageService messageService;

    private KeyboardService keyboardService;

    public ReminderTimeDetailsCommand(ReminderNotificationService reminderNotificationService, MessageBuilder messageBuilder,
                                      MessageService messageService, KeyboardService keyboardService) {
        this.reminderNotificationService = reminderNotificationService;
        this.messageBuilder = messageBuilder;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
    }

    @Override
    public String getName() {
        return MessagesProperties.REMINDER_TIME_DETAILS_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        ReminderNotification reminderNotification = reminderNotificationService.getReminderTime(requestParams.getInt(Arg.REMINDER_TIME_ID.getKey()));

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                messageBuilder.getReminderTimeMessage(reminderNotification),
                keyboardService.getReminderTimeKeyboard(requestParams.getInt(Arg.REMINDER_TIME_ID.getKey()), reminderNotification.getReminderId())
        );
    }
}
