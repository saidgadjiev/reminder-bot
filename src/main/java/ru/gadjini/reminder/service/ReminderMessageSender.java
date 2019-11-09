package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReminderMessageSender {

    private ReminderTextBuilder reminderTextBuilder;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private RemindMessageService remindMessageService;

    @Autowired
    public ReminderMessageSender(ReminderTextBuilder reminderTextBuilder,
                                 MessageService messageService,
                                 KeyboardService keyboardService,
                                 RemindMessageService remindMessageService) {
        this.reminderTextBuilder = reminderTextBuilder;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.remindMessageService = remindMessageService;
    }

    @Transactional
    public void sendRemindMessage(Reminder reminder) {
        String remindText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAtInReceiverTimeZone());
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
        }
        int messageId;

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageId = messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMIND,
                    new Object[]{remindText, UserUtils.userLink(reminder.getCreator())},
                    keyboardService.getReminderButtons(reminder.getId())
            ).getMessageId();
        } else {
            messageId = messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMIND_ME,
                    new Object[]{remindText},
                    keyboardService.getReminderButtons(reminder.getId())
            ).getMessageId();
        }
        remindMessageService.create(reminder.getId(), messageId);
    }

    @Transactional
    public void sendReminderComplete(String queryId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        String reminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAtInReceiverTimeZone());

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                    new Object[]{UserUtils.userLink(reminder.getReceiver()), reminderText}
            );
            messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);

            String receiverReminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAtInReceiverTimeZone());

            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED_FROM,
                    new Object[]{receiverReminderText, UserUtils.userLink(reminder.getCreator())}
            );
        } else {
            messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_ME_COMPLETED,
                    new Object[]{reminderText}
            );
        }
    }

    public void sendReminderCreated(Reminder reminder, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String reminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAtInReceiverTimeZone());
        int messageId;

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageId = messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_FROM,
                    new Object[]{UserUtils.userLink(reminder.getCreator()), reminderText}, keyboardService.getReminderButtons(reminder.getId())).getMessageId();
            messageService.sendMessageByCode(reminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_CREATED,
                    new Object[]{reminderText, UserUtils.userLink(reminder.getReceiver())}, replyKeyboardMarkup);
        } else {
            messageId = messageService.sendMessageByCode(reminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_ME_CREATED,
                    new Object[]{reminderText}, keyboardService.getReminderButtons(reminder.getId())).getMessageId();
        }
        remindMessageService.create(reminder.getId(), messageId);
    }

    public void sendReminders(long chatId, List<Reminder> reminders) {
        for (Reminder reminder : reminders) {
            String reminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAtInReceiverTimeZone());

            messageService.sendMessage(chatId, reminderText, keyboardService.reminderKeyboard(reminder.getId()));
        }
    }

    public void sendReminderTimeChanged(int messageId, UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = oldReminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(oldReminder.getId());
        }
        if (oldReminder.getReceiverId() != oldReminder.getCreatorId()) {
            messageService.sendMessage(
                    oldReminder.getReceiver().getChatId(),
                    reminderTextBuilder.changeReminderTime(
                            oldReminder.getText(),
                            oldReminder.getCreator(),
                            oldReminder.getRemindAtInReceiverTimeZone(),
                            updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone()
                    ),
                    null
            );
        }
        String newReminderText = reminderTextBuilder.create(oldReminder.getText(), updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone());
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboardService.reminderKeyboard(oldReminder.getId()));
        messageService.sendMessageByCode(oldReminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, replyKeyboard);
    }


    public void sendReminderPostponed(UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder reminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }
        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    reminderTextBuilder.postponeReminderTimeForCreator(
                            reminder.getText(),
                            reminder.getReceiver(),
                            updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone()
                    ),
                    replyKeyboard
            );

            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    reminderTextBuilder.postponeReminderTimeForReceiver(reminder.getText(), reminder.getCreator(), updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone()),
                    null
            );
        } else {
            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    reminderTextBuilder.postponeReminderForMe(reminder.getText(), updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone()),
                    replyKeyboard
            );
        }
    }

    public void sendReminderTextChanged(int messageId, UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = oldReminder.getRemindMessage();

        if (remindMessage != null) {
            String newReminderText = reminderTextBuilder.create(updateReminderResult.getNewReminder().getText(), updateReminderResult.getOldReminder().getRemindAtInReceiverTimeZone());

            messageService.editMessage(oldReminder.getReceiver().getChatId(), messageId, newReminderText, keyboardService.getReminderButtons(oldReminder.getId()));
        }
        if (oldReminder.getReceiverId() != oldReminder.getCreatorId()) {
            messageService.sendMessage(
                    oldReminder.getReceiver().getChatId(),
                    reminderTextBuilder.changeReminderText(
                            oldReminder.getText(),
                            updateReminderResult.getNewReminder().getText(),
                            oldReminder.getCreator()
                    ),
                    null
            );
        }
        String newReminderText = reminderTextBuilder.create(updateReminderResult.getNewReminder().getText(), updateReminderResult.getOldReminder().getRemindAtInReceiverTimeZone());
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboardService.reminderKeyboard(oldReminder.getId()));
        messageService.sendMessageByCode(oldReminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED, replyKeyboard);
    }

    public void sendReminderNotFound(long chatId, String queryId, int messageId) {
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_NOT_FOUND);
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_REMINDER_NOT_FOUND);
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderDeleted(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }
        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_DELETED_FROM,
                    new Object[]{UserUtils.userLink(reminder.getCreator()), reminder.getText()}
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_DELETED);
        messageService.deleteMessage(reminder.getCreator().getChatId(), messageId);
    }

    public void sendReminderCanceled(String queryId, int messageId, Reminder reminder) {
        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_FROM,
                    new Object[]{
                            reminder.getText(),
                            UserUtils.userLink(reminder.getCreator())
                    }
            );
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_CANCELED,
                    new Object[]{
                            UserUtils.userLink(reminder.getReceiver()),
                            reminder.getText()
                    }
            );
        } else {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_ME,
                    new Object[]{reminder.getText()}
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANCELED_ANSWER);
        messageService.deleteMessage(reminder.getReceiver().getChatId(), messageId);
        if (reminder.getRemindMessage() != null) {
            remindMessageService.delete(reminder.getId());
        }
    }

    public void sendCustomRemindCreated(long chatId, ZonedDateTime remindTime, ReplyKeyboardMarkup replyKeyboardMarkup) {
        messageService.sendMessage(chatId, reminderTextBuilder.customRemindText(remindTime), replyKeyboardMarkup);
    }
}
