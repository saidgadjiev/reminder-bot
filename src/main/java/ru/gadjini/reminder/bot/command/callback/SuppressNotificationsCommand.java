package ru.gadjini.reminder.bot.command.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.reminder.bot.command.api.CallbackBotCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.request.Arg;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageBuilder;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.util.KeyboardCustomizer;

import java.util.Collections;

@Component
public class SuppressNotificationsCommand implements CallbackBotCommand {

    private ReminderNotificationService reminderNotificationService;

    private ReminderService reminderService;

    private MessageService messageService;

    private ReminderMessageBuilder reminderMessageBuilder;

    private ReminderNotificationMessageBuilder reminderNotificationMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public SuppressNotificationsCommand(ReminderNotificationService reminderNotificationService, ReminderService reminderService,
                                        MessageService messageService, ReminderMessageBuilder reminderMessageBuilder,
                                        ReminderNotificationMessageBuilder reminderNotificationMessageBuilder, InlineKeyboardService inlineKeyboardService) {
        this.reminderNotificationService = reminderNotificationService;
        this.reminderService = reminderService;
        this.messageService = messageService;
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.reminderNotificationMessageBuilder = reminderNotificationMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public String getName() {
        return CommandNames.SUPPRESS_NOTIFICATIONS_COMMAND_NAME;
    }

    @Override
    public String processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int reminderId = requestParams.getInt(Arg.REMINDER_ID.getKey());
        reminderNotificationService.deleteCustomReminderNotifications(reminderId);
        if (!callbackQuery.getMessage().hasReplyMarkup()) {
            return null;
        }
        Reminder reminder = reminderService.getReminder(reminderId);

        KeyboardCustomizer keyboardCustomizer = new KeyboardCustomizer(callbackQuery.getMessage().getReplyMarkup());
        if (keyboardCustomizer.hasButton(CommandNames.SCHEDULE_COMMAND_NAME)
        || keyboardCustomizer.hasButton(CommandNames.REMINDER_DETAILS_COMMAND_NAME)) {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(callbackQuery.getMessage().getChatId())
                            .messageId(callbackQuery.getMessage().getMessageId())
                            .text(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getReceiverId())))
                            .replyKeyboard(keyboardCustomizer.remove(CommandNames.SUPPRESS_NOTIFICATIONS_COMMAND_NAME).getKeyboardMarkup())
            );
        } else {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(callbackQuery.getMessage().getChatId())
                            .messageId(callbackQuery.getMessage().getMessageId())
                            .text(reminderNotificationMessageBuilder.getReminderNotifications(Collections.emptyList(), reminder.getReceiver().getLocale()))
                            .replyKeyboard(inlineKeyboardService.getReminderTimesListKeyboard(Collections.emptyList(), reminderId, reminder.getReceiver().getLocale()))
            );
        }

        return null;
    }
}
