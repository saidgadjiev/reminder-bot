package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;

@Component
public class DeleteReminderTimeCommand implements CallbackBotCommand {

    private ReminderNotificationService reminderNotificationService;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    @Autowired
    public DeleteReminderTimeCommand(ReminderNotificationService reminderNotificationService,
                                     MessageService messageService, InlineKeyboardService inlineKeyboardService, LocalisationService localisationService) {
        this.reminderNotificationService = reminderNotificationService;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
    }

    @Override
    public String getName() {
        return CommandNames.DELETE_REMINDER_TIME_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderTimeId = requestParams.getInt(Arg.REMINDER_NOTIFICATION_ID.getKey());
        int reminderId = reminderNotificationService.deleteReminderNotification(reminderTimeId);

        messageService.editMessageAsync(
                EditMessageContext.from(callbackQuery)
                        .text(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_DELETED))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.SCHEDULE_COMMAND_NAME, new RequestParams().add(Arg.REMINDER_ID.getKey(), reminderId)))
        );

        return null;
    }
}
