package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserReminderNotificationScheduleCommand implements KeyboardBotCommand, NavigableBotCommand {

    //TODO:сотояние
    private Map<Long, Integer> messagesByChat = new ConcurrentHashMap<>();

    private String name;

    private UserReminderNotification.NotificationType notificationType;

    private final String historyName;

    private UserReminderNotificationService userReminderNotificationService;

    private ReminderNotificationMessageBuilder messageBuilder;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReplyKeyboardService replyKeyboardService;

    public UserReminderNotificationScheduleCommand(String name, String historyName,
                                                   UserReminderNotification.NotificationType notificationType,
                                                   UserReminderNotificationService userReminderNotificationService,
                                                   ReminderNotificationMessageBuilder messageBuilder,
                                                   MessageService messageService,
                                                   InlineKeyboardService inlineKeyboardService,
                                                   ReplyKeyboardService replyKeyboardService) {
        this.notificationType = notificationType;
        this.name = name;
        this.historyName = historyName;
        this.userReminderNotificationService = userReminderNotificationService;
        this.messageBuilder = messageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.replyKeyboardService = replyKeyboardService;
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
                messageBuilder.getUserReminderNotifications(userReminderNotifications),
                inlineKeyboardService.getUserReminderNotificationInlineKeyboard(userReminderNotifications.stream().map(UserReminderNotification::getId).collect(Collectors.toList()), notificationType)
        ).getMessageId();
        messageService.sendMessageByCode(
                message.getChatId(),
                MessagesProperties.MESSAGE_EDIT_USER_REMINDER_NOTIFICATION,
                replyKeyboardService.goBackCommand()
        );

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
                messageBuilder.getUserReminderNotifications(userReminderNotifications),
                inlineKeyboardService.getUserReminderNotificationInlineKeyboard(userReminderNotifications.stream().map(UserReminderNotification::getId).collect(Collectors.toList()), notificationType)
        );
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
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
