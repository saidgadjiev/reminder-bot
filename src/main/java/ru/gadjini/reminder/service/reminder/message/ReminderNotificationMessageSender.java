package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.RemindMessageService;
import ru.gadjini.reminder.time.DateTime;

@Service
public class ReminderNotificationMessageSender {

    private MessageService messageService;

    private ReminderNotificationMessageBuilder reminderNotificationMessageBuilder;

    private ReminderMessageBuilder reminderMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private RemindMessageService remindMessageService;

    @Autowired
    public ReminderNotificationMessageSender(MessageService messageService,
                                             ReminderNotificationMessageBuilder reminderNotificationMessageBuilder,
                                             ReminderMessageBuilder reminderMessageBuilder,
                                             InlineKeyboardService inlineKeyboardService,
                                             RemindMessageService remindMessageService) {
        this.messageService = messageService;
        this.reminderNotificationMessageBuilder = reminderNotificationMessageBuilder;
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.remindMessageService = remindMessageService;
    }

    public void sendRemindMessage(Reminder reminder, boolean itsTime) {
        sendRemindMessage(reminder, itsTime, null);
    }

    public void sendRemindMessage(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
        }

        String message;
        if (reminder.isMySelf()) {
            message = reminderNotificationMessageBuilder.getReminderNotificationMySelf(reminder, itsTime, nextRemindAt);
        } else {
            message = reminderNotificationMessageBuilder.getReminderNotificationForReceiver(reminder, itsTime, nextRemindAt);
        }

        InlineKeyboardMarkup keyboard = inlineKeyboardService.getRemindKeyboard(reminder);
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getReceiver().getChatId()).text(message).replyKeyboard(keyboard),
                msg -> remindMessageService.create(reminder.getId(), msg.getMessageId())
        );
    }

    public void sendCustomRemindCreatedFromReminderTimeDetails(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = reminderNotificationMessageBuilder.getReminderTimeMessage(customRemindResult.getReminderNotification());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(text)
                        .replyKeyboard(inlineKeyboardService.getReminderTimeKeyboard(customRemindResult.getReminderNotification().getId(), customRemindResult.getReminderNotification().getReminderId()))
        );
    }

    public void sendCustomRemindCreated(int userId, long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = reminderMessageBuilder.getReminderMessage(customRemindResult.getReminderNotification().getReminder());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(text + "\n\n" + reminderMessageBuilder.getCustomRemindText(customRemindResult))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, customRemindResult.getReminderNotification().getReminder()))
        );
    }
}
