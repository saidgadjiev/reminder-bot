package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.time.DateTime;

@Service
public class ReminderNotificationMessageSender {

    private MessageService messageService;

    private ReminderNotificationMessageBuilder reminderNotificationMessageBuilder;

    private ReminderMessageBuilder reminderMessageBuilder;

    private InlineKeyboardService inlineKeyboardService;

    private ReminderService reminderService;

    @Autowired
    public ReminderNotificationMessageSender(MessageService messageService,
                                             ReminderNotificationMessageBuilder reminderNotificationMessageBuilder,
                                             ReminderMessageBuilder reminderMessageBuilder,
                                             InlineKeyboardService inlineKeyboardService,
                                             ReminderService reminderService) {
        this.messageService = messageService;
        this.reminderNotificationMessageBuilder = reminderNotificationMessageBuilder;
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.inlineKeyboardService = inlineKeyboardService;
        this.reminderService = reminderService;
    }

    public void sendRemindMessage(Reminder reminder, boolean itsTime) {
        sendRemindMessage(reminder, itsTime, null);
    }

    public void sendRemindMessage(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (reminder.hasReceiverMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
        }

        String message;
        if (reminder.isMySelf()) {
            message = reminderNotificationMessageBuilder.getReminderNotificationMySelf(reminder, itsTime, nextRemindAt);
        } else {
            message = reminderNotificationMessageBuilder.getReminderNotificationForReceiver(reminder, itsTime, nextRemindAt);
        }

        InlineKeyboardMarkup keyboard = inlineKeyboardService.getRemindKeyboard(reminder);
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.HIGH).chatId(reminder.getReceiverId()).text(message).replyKeyboard(keyboard),
                msg -> reminderService.setReceiverMessage(reminder.getId(), msg.getMessageId())
        );
    }

    public void sendCustomRemindCreatedFromReminderTimeDetails(long chatId, int messageId, CustomRemindResult customRemindResult) {
        String text = reminderNotificationMessageBuilder.getReminderTimeMessage(customRemindResult.getReminderNotification());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(text)
                        .replyKeyboard(inlineKeyboardService.getReminderTimeKeyboard(customRemindResult.getReminderNotification().getId(), customRemindResult.getReminderNotification().getReminderId()))
        );
    }

    public void sendCustomRemindCreated(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard, CustomRemindResult customRemindResult) {
        Reminder reminder = customRemindResult.getReminderNotification().getReminder();
        String text = reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId());

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(text + "\n\n" + reminderMessageBuilder.getCustomRemindText(customRemindResult))
                        .replyKeyboard(replyKeyboard)
        );
    }
}
