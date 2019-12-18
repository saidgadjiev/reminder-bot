package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeleteUserReminderNotificationCommand implements CallbackBotCommand {

    private ReminderNotificationMessageBuilder reminderNotificationMessageBuilder;

    private KeyboardService keyboardService;

    private MessageService messageService;

    private UserReminderNotificationService userReminderNotificationService;

    @Autowired
    public DeleteUserReminderNotificationCommand( ReminderNotificationMessageBuilder reminderNotificationMessageBuilder,
                                                  KeyboardService keyboardService,
                                                  MessageService messageService,
                                                 UserReminderNotificationService userReminderNotificationService) {
        this.reminderNotificationMessageBuilder = reminderNotificationMessageBuilder;
        this.keyboardService = keyboardService;
        this.messageService = messageService;
        this.userReminderNotificationService = userReminderNotificationService;
    }

    @Override
    public String getName() {
        return MessagesProperties.DELETE_USER_REMINDER_NOTIFICATION_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        userReminderNotificationService.deleteById(requestParams.getInt(Arg.USER_REMINDER_NOTIFICATION_ID.getKey()));
        UserReminderNotification.NotificationType notificationType = UserReminderNotification.NotificationType.fromCode(requestParams.getInt(Arg.USER_REMINDER_NOTIFICATION_TYPE.getKey()));
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(callbackQuery.getFrom().getId(), notificationType);

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                reminderNotificationMessageBuilder.getUserReminderNotifications(userReminderNotifications),
                keyboardService.getUserReminderNotificationInlineKeyboard(userReminderNotifications.stream().map(UserReminderNotification::getId).collect(Collectors.toList()), notificationType)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(callbackQuery.getId(), MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION_DELETED);
    }
}