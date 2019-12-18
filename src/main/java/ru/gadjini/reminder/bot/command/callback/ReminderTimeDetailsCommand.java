package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;

@Component
public class ReminderTimeDetailsCommand implements CallbackBotCommand {

    private ReminderNotificationService reminderNotificationService;

    private ReminderNotificationMessageBuilder messageBuilder;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public ReminderTimeDetailsCommand(ReminderNotificationService reminderNotificationService,
                                      ReminderNotificationMessageBuilder messageBuilder,
                                      MessageService messageService,
                                      InlineKeyboardService inlineKeyboardService) {
        this.reminderNotificationService = reminderNotificationService;
        this.messageBuilder = messageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public String getName() {
        return MessagesProperties.REMINDER_TIME_DETAILS_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        ReminderNotification reminderNotification = reminderNotificationService.getReminderTime(requestParams.getInt(Arg.REMINDER_NOTIFICATION_ID.getKey()));

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                messageBuilder.getReminderTimeMessage(reminderNotification),
                inlineKeyboardService.getReminderTimeKeyboard(requestParams.getInt(Arg.REMINDER_NOTIFICATION_ID.getKey()), reminderNotification.getReminderId())
        );
    }
}
