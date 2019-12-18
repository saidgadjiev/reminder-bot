package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReminderTimeScheduleCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderNotificationService reminderNotificationService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private TimeBuilder timeBuilder;

    @Autowired
    public ReminderTimeScheduleCommand(ReminderNotificationService reminderNotificationService, MessageService messageService, InlineKeyboardService inlineKeyboardService, TimeBuilder timeBuilder) {
        this.reminderNotificationService = reminderNotificationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.timeBuilder = timeBuilder;
    }

    @Override
    public String getName() {
        return MessagesProperties.SCHEDULE_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<ReminderNotification> reminderNotifications = reminderNotificationService.getList(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                message(reminderNotifications),
                inlineKeyboardService.getReminderTimesListKeyboard(reminderNotifications.stream().map(ReminderNotification::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey()))
        );
    }

    @Override
    public String getHistoryName() {
        return MessagesProperties.SCHEDULE_COMMAND_NAME;
    }

    @Override
    public void restore(long chatId, int messageId, String queryId, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<ReminderNotification> reminderNotifications = reminderNotificationService.getList(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                chatId,
                messageId,
                message(reminderNotifications),
                inlineKeyboardService.getReminderTimesListKeyboard(reminderNotifications.stream().map(ReminderNotification::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey()))
        );
    }

    private String message(List<ReminderNotification> reminderNotifications) {
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (ReminderNotification reminderNotification : reminderNotifications) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(timeBuilder.time(reminderNotification));
        }

        return message.toString();
    }
}
