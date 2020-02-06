package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.time.DateTime;

import java.util.Locale;
import java.util.stream.Collectors;

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

    public void sendCustomRemindCreatedFromReminderTimeDetails(long chatId, int messageId, CustomRemindResult customRemindResult, Locale locale) {
        String text = reminderNotificationMessageBuilder.getReminderTimeMessage(customRemindResult.getReminderNotifications(), locale);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(text)
                        .replyKeyboard(inlineKeyboardService.getReminderTimesListKeyboard(
                                customRemindResult.getReminderNotifications().stream().map(ReminderNotification::getId).collect(Collectors.toList()),
                                customRemindResult.getReminder().getId(), null)
                        )
        );
    }

    public void sendCustomRemindCreated(long chatId, int messageId, InlineKeyboardMarkup replyKeyboard, CustomRemindResult customRemindResult) {
        Reminder reminder = customRemindResult.getReminder();
        String text = reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.Config().receiverId(reminder.getReceiverId()));

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(text + "\n\n" + reminderMessageBuilder.getCustomRemindText(customRemindResult))
                        .replyKeyboard(replyKeyboard)
        );
    }
}
