package ru.gadjini.reminder.service.reminder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.keyboard.KeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.security.SecurityService;
import ru.gadjini.reminder.util.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderMessageSender {

    private ReminderTextBuilder reminderTextBuilder;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private RemindMessageService remindMessageService;

    private SecurityService securityService;

    private LocalisationService localisationService;

    @Autowired
    public ReminderMessageSender(ReminderTextBuilder reminderTextBuilder,
                                 MessageService messageService,
                                 KeyboardService keyboardService,
                                 RemindMessageService remindMessageService,
                                 SecurityService securityService,
                                 LocalisationService localisationService) {
        this.reminderTextBuilder = reminderTextBuilder;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.remindMessageService = remindMessageService;
        this.securityService = securityService;
        this.localisationService = localisationService;
    }

    @Transactional
    public void sendRemindMessage(Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
        }
        int messageId;

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageId = messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    reminderTextBuilder.remindReceiver(reminder),
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), null)
            ).getMessageId();
        } else {
            messageId = messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    reminderTextBuilder.remindMe(reminder),
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), null)
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

        String reminderText = reminderTextBuilder.create(reminder);

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                    new Object[]{UserUtils.userLink(reminder.getReceiver()), reminderText}
            );
            messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);

            String receiverReminderText = reminderTextBuilder.create(reminder);

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

    @Transactional
    public void sendReminderCompleteFromList(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        String reminderText = reminderTextBuilder.create(reminder);

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                    new Object[]{UserUtils.userLink(reminder.getReceiver()), reminderText}
            );
            messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);

            String receiverReminderText = reminderTextBuilder.create(reminder);

            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED_FROM,
                    new Object[]{receiverReminderText, UserUtils.userLink(reminder.getCreator())},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_ME_COMPLETED,
                    new Object[]{reminderText},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        }
    }

    public void sendReminderCreated(Reminder reminder, ReplyKeyboardMarkup replyKeyboardMarkup) {
        int messageId;

        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            messageId = messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    reminderTextBuilder.reminderCreatedReceiver(reminder),
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), null)).getMessageId();
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    reminderTextBuilder.reminderCreatedCreator(reminder),
                    replyKeyboardMarkup);
        } else {
            messageId = messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    reminderTextBuilder.reminderCreatedMe(reminder),
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), null)).getMessageId();
        }
        remindMessageService.create(reminder.getId(), messageId);
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
        String newReminderText = reminderTextBuilder.create(oldReminder.getText(), updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone(), oldReminder.getNote());

        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboardService.getEditReminderKeyboard(oldReminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME));
    }

    public void sendReminderPostponed(UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder reminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    remindMessage.getMessageId(),
                    reminderTextBuilder.postponeReminderForReceiver(
                            reminder.getText(),
                            updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone()
                    ) + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED),
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), null)
            );
        }
        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    reminderTextBuilder.postponeReminderTimeForCreator(
                            reminder.getText(),
                            reminder.getReceiver(),
                            updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone()
                    ),
                    null
            );
        }
    }

    public void sendReminderPostponedFromList(long chatId, int messageId, UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder reminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            String text = reminderTextBuilder.postponeReminderForReceiver(reminder.getText(), updateReminderResult.getNewReminder().getRemindAtInReceiverTimeZone());
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    remindMessage.getMessageId(),
                    text,
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), null)
            );
            messageService.editMessage(
                    chatId,
                    messageId,
                    text,
                    keyboardService.getReminderDetailsKeyboard(securityService.getAuthenticatedUser().getId(), reminder)
            );
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
        }
    }

    public void sendReminderTextChanged(int messageId, UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = oldReminder.getRemindMessage();
        String newReminderText = reminderTextBuilder.create(updateReminderResult.getNewReminder().getText(), oldReminder.getRemindAtInReceiverTimeZone(), oldReminder.getNote());

        if (remindMessage != null) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(oldReminder.getId());
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
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboardService.getEditReminderKeyboard(oldReminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME));
    }

    public void sendReminderNotFound(long chatId, String queryId, int messageId) {
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_NOT_FOUND);
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_REMINDER_NOT_FOUND);
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompleted(long chatId, String queryId, int messageId) {
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED);
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED);
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompletedFromList(long chatId, String queryId, int messageId) {
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED);
        messageService.editMessageByMessageCode(chatId, messageId, MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED, keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME));
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
        messageService.editMessageByMessageCode(
                reminder.getCreator().getChatId(),
                messageId,
                MessagesProperties.MESSAGE_REMINDER_DELETED,
                keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
        );
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

    public void sendReminderCanceledFromList(String queryId, int messageId, Reminder reminder) {
        if (reminder.getReceiverId() != reminder.getCreatorId()) {
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_FROM,
                    new Object[]{
                            reminder.getText(),
                            UserUtils.userLink(reminder.getCreator())
                    },
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
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
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_ME,
                    new Object[]{reminder.getText()},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANCELED_ANSWER);
        if (reminder.getRemindMessage() != null) {
            remindMessageService.delete(reminder.getId());
        }
    }

    public void sendCustomRemindCreated(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = reminderTextBuilder.create(customRemindResult.getReminder());

        messageService.editMessage(
                chatId,
                messageId,
                text + "\n\n" + reminderTextBuilder.customRemindText(customRemindResult.getZonedDateTime()),
                keyboardService.getReceiverReminderKeyboard(customRemindResult.getReminder().getId(), null)
        );
    }

    public void sendCustomRemindCreatedFromList(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = reminderTextBuilder.create(customRemindResult.getReminder());

        messageService.editMessage(
                chatId,
                messageId,
                text + "\n\n" + reminderTextBuilder.customRemindText(customRemindResult.getZonedDateTime()),
                keyboardService.getReminderDetailsKeyboard(securityService.getAuthenticatedUser().getId(), customRemindResult.getReminder())
        );
    }

    public void sendCompletedReminders(long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageByMessageCode(
                    chatId,
                    messageId,
                    MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY,
                    keyboardService.getEmptyRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        } else {
            User user = securityService.getAuthenticatedUser();

            String text = reminderTextBuilder.remindersList(user.getId(), reminders);

            messageService.editMessage(
                    chatId,
                    messageId,
                    text,
                    keyboardService.getCompletedRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        }
    }

    public void sendActiveReminders(long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageByMessageCode(
                    chatId,
                    messageId,
                    MessagesProperties.MESSAGE_ACTIVE_REMINDERS_EMPTY,
                    keyboardService.getEmptyRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        } else {
            User user = securityService.getAuthenticatedUser();

            String text = reminderTextBuilder.remindersList(user.getId(), reminders);

            messageService.editMessage(
                    chatId,
                    messageId,
                    text,
                    keyboardService.getActiveRemindersListKeyboard(reminders.stream().map(Reminder::getId).collect(Collectors.toList()), MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        }
    }

    public void sendReminderEdit(Long chatId, Integer messageId, int reminderId) {
        messageService.editReplyKeyboard(
                chatId,
                messageId,
                keyboardService.getEditReminderKeyboard(reminderId, MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)
        );
    }

    public void sendReminderDetails(Long chatId, Integer messageId, Reminder reminder) {
        String text = reminderTextBuilder.create(reminder);
        User user = securityService.getAuthenticatedUser();

        messageService.editMessage(
                chatId,
                messageId,
                text,
                keyboardService.getReminderDetailsKeyboard(user.getId(), reminder)
        );
    }

    public void sendCompletedRemindersDeleted(long chatId, int messageId) {
        messageService.editMessageByMessageCode(
                chatId,
                messageId,
                MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY,
                keyboardService.getEmptyRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
        );
    }

    public void sendReminderNoteChanged(Reminder reminder, int messageId, ReplyKeyboard replyKeyboard) {
        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            String text = reminderTextBuilder.changeReminderNoteReceiver(reminder.getText(), reminder.getNote(), reminder.getCreator(), reminder.getRemindAtInReceiverTimeZone());

            messageService.sendMessage(reminder.getReceiver().getChatId(), text, null);
        }
        String reminderText = reminderTextBuilder.create(reminder);

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderText,
                keyboardService.getEditReminderKeyboard(reminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)
        );
    }

    public void sendReminderNoteDeleted(String queryId, int messageId, Reminder reminder) {
        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            String text = reminderTextBuilder.deleteReminderNoteReceiver(reminder.getText(), reminder.getCreator(), reminder.getRemindAtInReceiverTimeZone());

            messageService.sendMessage(reminder.getReceiver().getChatId(), text, null);
        }
        String reminderText = reminderTextBuilder.create(reminder);

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderText,
                keyboardService.getReminderDetailsKeyboard(securityService.getAuthenticatedUser().getId(), reminder)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED);
    }
}
