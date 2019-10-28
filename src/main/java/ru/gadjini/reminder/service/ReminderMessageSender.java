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
    public void sendReminderComplete(String queryId, int messageId, Reminder reminder) {
        if (reminder == null) {
            messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
            messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
            messageService.deleteMessage(reminder.getReceiver().getChatId(), messageId);
            return;
        }

        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
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

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_FROM,
                    new Object[]{UserUtils.userLink(reminder.getCreator()), reminderText});
            messageService.sendMessageByCode(reminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_CREATED,
                    new Object[]{reminderText, UserUtils.userLink(reminder.getReceiver())}, replyKeyboardMarkup);
        } else {
            messageService.sendMessageByCode(reminder.getCreator().getChatId(), MessagesProperties.MESSAGE_REMINDER_ME_CREATED,
                    new Object[]{reminderText});
        }
    }

    public void sendReminders(long chatId, List<Reminder> reminders) {
        for (Reminder reminder : reminders) {
            String reminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAtInReceiverTimeZone());

            messageService.sendMessage(chatId, reminderText, keyboardService.reminderKeyboard(reminder.getId()));
        }
    }

    public void sendReminderTimeChanged(long currChatId, int messageId, UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = oldReminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), remindMessage.getMessageId());
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
        messageService.editMessage(currChatId, messageId, newReminderText, keyboardService.reminderKeyboard(oldReminder.getId()));
        messageService.sendMessageByCode(currChatId, MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, replyKeyboard);
    }
}
