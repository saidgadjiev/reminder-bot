package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReminderTimeScheduleCommand implements CallbackBotCommand, NavigableCallbackBotCommand {

    private ReminderNotificationService reminderNotificationService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderNotificationMessageBuilder reminderNotificationMessageBuilder;

    @Autowired
    public ReminderTimeScheduleCommand(ReminderNotificationService reminderNotificationService, MessageService messageService, InlineKeyboardService inlineKeyboardService, ReminderNotificationMessageBuilder reminderNotificationMessageBuilder) {
        this.reminderNotificationService = reminderNotificationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderNotificationMessageBuilder = reminderNotificationMessageBuilder;
    }

    @Override
    public String getName() {
        return CommandNames.SCHEDULE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<ReminderNotification> reminderNotifications = reminderNotificationService.getCustomRemindersList(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                EditMessageContext.from(callbackQuery)
                        .text(reminderNotificationMessageBuilder.getReminderNotifications(reminderNotifications))
                        .replyKeyboard(inlineKeyboardService.getReminderTimesListKeyboard(reminderNotifications.stream().map(ReminderNotification::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey())))
        );
        return null;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.SCHEDULE_COMMAND_NAME;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<ReminderNotification> reminderNotifications = reminderNotificationService.getCustomRemindersList(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessage(
                new EditMessageContext()
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(reminderNotificationMessageBuilder.getReminderNotifications(reminderNotifications))
                        .replyKeyboard(inlineKeyboardService.getReminderTimesListKeyboard(reminderNotifications.stream().map(ReminderNotification::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey())))
        );
    }
}
