package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.MessageBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserReminderNotificationScheduleCommand implements KeyboardBotCommand, NavigableBotCommand {

    private Map<Long, Integer> messagesByChat = new ConcurrentHashMap<>();

    private String name;

    private UserReminderNotification.NotificationType notificationType;

    private final String historyName;

    private UserReminderNotificationService userReminderNotificationService;

    private MessageBuilder messageBuilder;

    private MessageService messageService;

    private KeyboardService keyboardService;

    public UserReminderNotificationScheduleCommand(String name, String historyName,
                                                   UserReminderNotification.NotificationType notificationType,
                                                   UserReminderNotificationService userReminderNotificationService,
                                                   MessageBuilder messageBuilder,
                                                   MessageService messageService,
                                                   KeyboardService keyboardService) {
        this.notificationType = notificationType;
        this.name = name;
        this.historyName = historyName;
        this.userReminderNotificationService = userReminderNotificationService;
        this.messageBuilder = messageBuilder;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
    }

    @Override
    public boolean canHandle(String command) {
        return name.equals(command);
    }

    @Override
    public void processMessage(Message message) {
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(message.getFrom().getId(), notificationType);

        int messageId = messageService.sendMessage(
                message.getChatId(),
                messageBuilder.getUserReminderNotificationsMessage(userReminderNotifications),
                keyboardService.getUserReminderNotificationInlineKeyboard(userReminderNotifications.stream().map(UserReminderNotification::getId).collect(Collectors.toList()), notificationType)
        ).getMessageId();

        messagesByChat.put(message.getChatId(), messageId);
    }

    @Override
    public void processNonCommandUpdate(Message message) {
        userReminderNotificationService.create(message.getText().trim(), notificationType);
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getList(message.getFrom().getId(), notificationType);
        int messageId = messagesByChat.get(message.getChatId());

        messageService.editMessage(
                message.getChatId(),
                messageId,
                messageBuilder.getUserReminderNotificationsMessage(userReminderNotifications),
                keyboardService.getUserReminderNotificationInlineKeyboard(userReminderNotifications.stream().map(UserReminderNotification::getId).collect(Collectors.toList()), notificationType)
        );
    }

    @Override
    public String getHistoryName() {
        return historyName;
    }

    @Override
    public String getParentHistoryName() {
        return MessagesProperties.USER_REMINDER_NOTIFICATION_HISTORY_NAME;
    }
}
