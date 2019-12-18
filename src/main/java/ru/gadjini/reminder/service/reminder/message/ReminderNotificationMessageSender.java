package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.RemindMessageService;
import ru.gadjini.reminder.time.DateTime;

@Service
public class ReminderNotificationMessageSender {

    private MessageService messageService;

    private ReminderNotificationMessageBuilder reminderNotificationMessageBuilder;

    private ReminderMessageBuilder reminderMessageBuilder;

    private KeyboardService keyboardService;

    private RemindMessageService remindMessageService;

    @Autowired
    public ReminderNotificationMessageSender(MessageService messageService,
                                             ReminderNotificationMessageBuilder reminderNotificationMessageBuilder,
                                             ReminderMessageBuilder reminderMessageBuilder,
                                             KeyboardService keyboardService,
                                             RemindMessageService remindMessageService) {
        this.messageService = messageService;
        this.reminderNotificationMessageBuilder = reminderNotificationMessageBuilder;
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.keyboardService = keyboardService;
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

        InlineKeyboardMarkup keyboard = keyboardService.getRemindKeyboard(reminder.getId(), itsTime, reminder.isRepeatable());
        int messageId = messageService.sendMessage(reminder.getReceiver().getChatId(), message, keyboard).getMessageId();
        remindMessageService.create(reminder.getId(), messageId);
    }

    public void sendCustomRemindCreatedFromReminderTimeDetails(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = reminderNotificationMessageBuilder.getReminderTimeMessage(customRemindResult.getReminderNotification());

        messageService.editMessage(
                chatId,
                messageId,
                text,
                keyboardService.getReminderTimeKeyboard(customRemindResult.getReminderNotification().getId(), customRemindResult.getReminderNotification().getReminderId())
        );
    }

    public void sendCustomRemindCreated(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = reminderMessageBuilder.getReminderMessage(customRemindResult.getReminderNotification().getReminder());

        messageService.editMessage(
                chatId,
                messageId,
                text + "\n\n" + reminderMessageBuilder.getCustomRemindText(customRemindResult),
                keyboardService.getReceiverReminderKeyboard(customRemindResult.getReminderNotification().getReminderId(), customRemindResult.getReminderNotification().getReminder().isRepeatable())
        );
    }
}
