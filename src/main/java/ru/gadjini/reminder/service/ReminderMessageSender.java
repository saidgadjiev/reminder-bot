package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.util.UserUtils;

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
        String remindText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAt());
        RemindMessage remindMessage = remindMessageService.getByReminderId(reminder.getId());

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
        RemindMessage remindMessage = remindMessageService.getByReminderId(reminder.getId());

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
        }

        String reminderText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAt());

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                    new Object[]{UserUtils.userLink(reminder.getReceiver()), reminderText}
            );
            messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED_FROM,
                    new Object[]{reminderText, UserUtils.userLink(reminder.getCreator())}
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
}
