package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.bot.command.api.NavigableCallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
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

    private TgUserService userService;

    @Autowired
    public ReminderTimeScheduleCommand(ReminderNotificationService reminderNotificationService, MessageService messageService,
                                       InlineKeyboardService inlineKeyboardService, ReminderNotificationMessageBuilder reminderNotificationMessageBuilder,
                                       TgUserService userService) {
        this.reminderNotificationService = reminderNotificationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderNotificationMessageBuilder = reminderNotificationMessageBuilder;
        this.userService = userService;
    }

    @Override
    public String getName() {
        return CommandNames.SCHEDULE_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        List<ReminderNotification> reminderNotifications = reminderNotificationService.getCustomRemindersList(requestParams.getInt(Arg.REMINDER_ID.getKey()));
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(reminderNotificationMessageBuilder.getReminderNotifications(reminderNotifications, userService.getLocale(callbackQuery.getFrom().getId())))
                        .replyKeyboard(inlineKeyboardService.getReminderTimesListKeyboard(reminderNotifications.stream().map(ReminderNotification::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey()), null))
        );
        return null;
    }

    @Override
    public void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {
        List<ReminderNotification> reminderNotifications = reminderNotificationService.getCustomRemindersList(requestParams.getInt(Arg.REMINDER_ID.getKey()));

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(tgMessage.getChatId())
                        .messageId(tgMessage.getMessageId())
                        .text(reminderNotificationMessageBuilder.getReminderNotifications(reminderNotifications, userService.getLocale(tgMessage.getUser().getId())))
                        .replyKeyboard(inlineKeyboardService.getReminderTimesListKeyboard(reminderNotifications.stream().map(ReminderNotification::getId).collect(Collectors.toList()), requestParams.getInt(Arg.REMINDER_ID.getKey()), null))
        );
    }
}
