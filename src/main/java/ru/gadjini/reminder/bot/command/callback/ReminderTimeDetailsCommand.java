package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;

import java.util.Locale;

@Component
public class ReminderTimeDetailsCommand implements CallbackBotCommand {

    private ReminderNotificationService reminderNotificationService;

    private ReminderNotificationMessageBuilder messageBuilder;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private TgUserService userService;

    @Autowired
    public ReminderTimeDetailsCommand(ReminderNotificationService reminderNotificationService,
                                      ReminderNotificationMessageBuilder messageBuilder,
                                      MessageService messageService,
                                      InlineKeyboardService inlineKeyboardService, TgUserService userService) {
        this.reminderNotificationService = reminderNotificationService;
        this.messageBuilder = messageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.userService = userService;
    }

    @Override
    public String getName() {
        return CommandNames.REMINDER_TIME_DETAILS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        ReminderNotification reminderNotification = reminderNotificationService.getReminderTime(requestParams.getInt(Arg.REMINDER_NOTIFICATION_ID.getKey()));

        Locale locale = userService.getLocale(callbackQuery.getFrom().getId());
        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(messageBuilder.getReminderTimeMessage(reminderNotification, locale))
                        .replyKeyboard(inlineKeyboardService.getReminderTimeKeyboard(requestParams.getInt(Arg.REMINDER_NOTIFICATION_ID.getKey()), reminderNotification.getReminderId(), locale))
        );
        return null;
    }
}
