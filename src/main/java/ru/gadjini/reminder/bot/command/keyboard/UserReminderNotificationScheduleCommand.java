package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class UserReminderNotificationScheduleCommand implements KeyboardBotCommand, NavigableBotCommand {

    private CommandStateService stateService;

    private String name;

    private UserReminderNotification.NotificationType notificationType;

    private final String historyName;

    private UserReminderNotificationService userReminderNotificationService;

    private ReminderNotificationMessageBuilder messageBuilder;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReplyKeyboardService replyKeyboardService;

    private LocalisationService localisationService;

    public UserReminderNotificationScheduleCommand(String name, String historyName,
                                                   UserReminderNotification.NotificationType notificationType,
                                                   UserReminderNotificationService userReminderNotificationService,
                                                   ReminderNotificationMessageBuilder messageBuilder,
                                                   MessageService messageService, InlineKeyboardService inlineKeyboardService,
                                                   CurrReplyKeyboard replyKeyboardService, CommandStateService stateService,
                                                   LocalisationService localisationService) {
        this.stateService = stateService;
        this.notificationType = notificationType;
        this.name = name;
        this.historyName = historyName;
        this.userReminderNotificationService = userReminderNotificationService;
        this.messageBuilder = messageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.replyKeyboardService = replyKeyboardService;
        this.localisationService = localisationService;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getNonCachedList(message.getFrom().getId(), notificationType);

        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(message.getChatId())
                        .text(messageBuilder.getUserReminderNotifications(userReminderNotifications))
                        .replyKeyboard(inlineKeyboardService.getUserReminderNotificationInlineKeyboard(userReminderNotifications.stream().map(UserReminderNotification::getId).collect(Collectors.toList()), notificationType)),
                msg -> stateService.setState(msg.getChatId(), msg.getMessageId())
        );
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(message.getChatId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_EDIT_USER_REMINDER_NOTIFICATION))
                        .replyKeyboard(replyKeyboardService.goBackCommand(message.getChatId()))
        );
        return true;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        userReminderNotificationService.create(message.getFrom().getId(), text, notificationType);
        List<UserReminderNotification> userReminderNotifications = userReminderNotificationService.getNonCachedList(message.getFrom().getId(), notificationType);
        int messageId = stateService.getState(message.getChatId());

        messageService.editMessage(
                new EditMessageContext()
                        .chatId(message.getChatId())
                        .messageId(messageId)
                        .text(messageBuilder.getUserReminderNotifications(userReminderNotifications))
                        .replyKeyboard(inlineKeyboardService.getUserReminderNotificationInlineKeyboard(userReminderNotifications.stream().map(UserReminderNotification::getId).collect(Collectors.toList()), notificationType))
        );
        messageService.deleteMessage(message.getChatId(), message.getMessageId());
    }

    @Override
    public void leave(long chatId) {
        stateService.deleteState(chatId);
    }

    @Override
    public String getHistoryName() {
        return historyName;
    }

    @Override
    public String getParentHistoryName() {
        return CommandNames.USER_REMINDER_NOTIFICATION_HISTORY_NAME;
    }
}
