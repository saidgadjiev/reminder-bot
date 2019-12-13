package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.UserUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderMessageSender {

    private MessageBuilder messageBuilder;

    private MessageService messageService;

    private KeyboardService keyboardService;

    private RemindMessageService remindMessageService;

    private SecurityService securityService;

    private LocalisationService localisationService;

    @Autowired
    public ReminderMessageSender(MessageBuilder messageBuilder,
                                 MessageService messageService,
                                 KeyboardService keyboardService,
                                 RemindMessageService remindMessageService,
                                 SecurityService securityService,
                                 LocalisationService localisationService) {
        this.messageBuilder = messageBuilder;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
        this.remindMessageService = remindMessageService;
        this.securityService = securityService;
        this.localisationService = localisationService;
    }

    @Transactional
    public void sendRemindMessage(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
        }

        String message;
        if (reminder.isMySelf()) {
            message = messageBuilder.getRemindMySelf(reminder, itsTime, nextRemindAt.withZoneSameInstant(reminder.getReceiverZoneId()));
        } else {
            message = messageBuilder.getRemindForReceiver(reminder, itsTime, nextRemindAt);
        }

        InlineKeyboardMarkup keyboard = keyboardService.getReceiverReminderKeyboard(reminder.getId(), null);
        int messageId = messageService.sendMessage(reminder.getReceiver().getChatId(), message, keyboard).getMessageId();
        remindMessageService.create(reminder.getId(), messageId);
    }

    @Transactional
    public void sendReminderCompleted(String queryId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        String reminderText = messageBuilder.getReminderMessage(reminder);

        if (reminder.isMySelf()) {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_ME_COMPLETED,
                    new Object[]{reminderText}
            );
        } else {
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED_CREATOR,
                    new Object[]{UserUtils.userLink(reminder.getReceiver()), reminderText}
            );
            String receiverReminderText = messageBuilder.getReminderMessage(reminder);

            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED_RECEIVER,
                    new Object[]{receiverReminderText, UserUtils.userLink(reminder.getCreator())}
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

    @Transactional
    public void sendReminderCompletedFromList(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        String reminderText = messageBuilder.getReminderMessage(reminder);

        if (reminder.isMySelf()) {
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_ME_COMPLETED,
                    new Object[]{reminderText},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED_CREATOR,
                    new Object[]{UserUtils.userLink(reminder.getReceiver()), reminderText}
            );
            String receiverReminderText = messageBuilder.getReminderMessage(reminder);

            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED_RECEIVER,
                    new Object[]{receiverReminderText, UserUtils.userLink(reminder.getCreator())},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

    public void sendReminderCreated(Reminder reminder, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String messageForCreator;

        if (reminder.isMySelf()) {
            messageForCreator = messageBuilder.getMySelfReminderCreated(reminder);
        } else {
            messageForCreator = messageBuilder.getReminderCreatedForReceiver(reminder);
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    messageBuilder.getReminderCreatedForCreator(reminder),
                    replyKeyboardMarkup);
        }
        InlineKeyboardMarkup keyboard = keyboardService.getReceiverReminderKeyboard(reminder.getId(), null);
        int messageId = messageService.sendMessage(reminder.getReceiver().getChatId(), messageForCreator, keyboard).getMessageId();

        remindMessageService.create(reminder.getId(), messageId);
    }

    public void sendReminderTimeChanged(int messageId, UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = oldReminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(oldReminder.getId());
        }

        if (!oldReminder.isMySelf()) {
            String message = messageBuilder.getReminderTimeChanged(
                    oldReminder.getText(),
                    oldReminder.getCreator(),
                    oldReminder.getRemindAtInReceiverZone(),
                    updateReminderResult.getNewReminder().getRemindAtInReceiverZone()
            );
            messageService.sendMessage(oldReminder.getReceiver().getChatId(), message, null);
        }
        String newReminderText = messageBuilder.getReminderMessage(updateReminderResult.getNewReminder());
        InlineKeyboardMarkup keyboard = keyboardService.getEditReminderKeyboard(oldReminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME);
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboard);
    }

    public void sendReminderPostponed(UpdateReminderResult updateReminderResult, String reason, ReplyKeyboard replyKeyboard) {
        Reminder reminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    remindMessage.getMessageId(),
                    messageBuilder.getReminderPostponedForReceiver(
                            reminder.getText(),
                            updateReminderResult.getNewReminder().getRemindAtInReceiverZone()
                    ) + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED),
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), null)
            );
        }
        if (!reminder.isMySelf()) {
            StringBuilder messageBuilder = new StringBuilder();

            messageBuilder.append(this.messageBuilder.getReminderPostponedForCreator(
                    reminder.getText(),
                    reminder.getReceiver(),
                    updateReminderResult.getNewReminder().getRemindAtInReceiverZone()
            ));

            if (StringUtils.isNotBlank(reason)) {
                messageBuilder.append("\n\n").append(reason);
            }

            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    messageBuilder.toString(),
                    null
            );
        }
        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_POSTPONED, replyKeyboard);
    }

    public void sendReminderTextChanged(int messageId, UpdateReminderResult updateReminderResult, ReplyKeyboard replyKeyboard) {
        Reminder oldReminder = updateReminderResult.getOldReminder();

        RemindMessage remindMessage = oldReminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(oldReminder.getId());
        }

        if (!oldReminder.isMySelf()) {
            String message = messageBuilder.getReminderTextChanged(
                    oldReminder.getText(),
                    updateReminderResult.getNewReminder().getText(),
                    oldReminder.getCreator()
            );
            messageService.sendMessage(oldReminder.getReceiver().getChatId(), message, null);
        }
        String newReminderText = messageBuilder.getReminderMessage(updateReminderResult.getNewReminder());
        InlineKeyboardMarkup keyboard = keyboardService.getEditReminderKeyboard(oldReminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME);
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboard);
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

        if (!reminder.isMySelf()) {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_DELETED_RECEIVER,
                    new Object[]{UserUtils.userLink(reminder.getCreator()), reminder.getText()}
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_DELETED_CREATOR);
        messageService.editMessageByMessageCode(
                reminder.getCreator().getChatId(),
                messageId,
                MessagesProperties.MESSAGE_REMINDER_DELETED_CREATOR,
                keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
        );
    }

    public void sendReminderCanceled(String queryId, Reminder reminder) {
        if (reminder.getRemindMessage() != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_ME,
                    new Object[]{reminder.getText()}
            );
        } else {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_RECEIVER,
                    new Object[]{
                            reminder.getText(),
                            UserUtils.userLink(reminder.getCreator())
                    }
            );
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_CREATOR,
                    new Object[]{
                            UserUtils.userLink(reminder.getReceiver()),
                            reminder.getText()
                    }
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANCELED_ANSWER);
    }

    public void sendReminderCanceledFromList(String queryId, int messageId, Reminder reminder) {
        if (reminder.getRemindMessage() != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_ME,
                    new Object[]{reminder.getText()},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_RECEIVER,
                    new Object[]{
                            reminder.getText(),
                            UserUtils.userLink(reminder.getCreator())
                    },
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
            messageService.sendMessageByCode(
                    reminder.getCreator().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_CANCELED_CREATOR,
                    new Object[]{
                            UserUtils.userLink(reminder.getReceiver()),
                            reminder.getText()
                    }
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANCELED_ANSWER);
    }

    public void sendCustomRemindCreated(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = messageBuilder.getReminderMessage(customRemindResult.getReminder());

        messageService.editMessage(
                chatId,
                messageId,
                text + "\n\n" + messageBuilder.getCustomRemindText(customRemindResult.getZonedDateTime()),
                keyboardService.getReceiverReminderKeyboard(customRemindResult.getReminder().getId(), null)
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

            String text = messageBuilder.getRemindersListInfo(user.getId(), reminders);

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

            String text = messageBuilder.getRemindersListInfo(user.getId(), reminders);

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
        String text = messageBuilder.getReminderMessage(reminder);
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
        if (!reminder.isMySelf()) {
            String text = messageBuilder.getReminderNoteChangedForReceiver(reminder.getText(), reminder.getNote(), reminder.getCreator(), reminder.getRemindAtInReceiverZone());

            messageService.sendMessage(reminder.getReceiver().getChatId(), text, null);
        }
        String reminderText = messageBuilder.getReminderMessage(reminder);

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderText,
                keyboardService.getEditReminderKeyboard(reminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)
        );
    }

    public void sendReminderNoteDeleted(String queryId, int messageId, Reminder reminder) {
        if (!reminder.isMySelf()) {
            String text = messageBuilder.getReminderNoteDeletedForReceiver(reminder.getText(), reminder.getCreator(), reminder.getRemindAtInReceiverZone());

            messageService.sendMessage(reminder.getReceiver().getChatId(), text, null);
        }
        String reminderText = messageBuilder.getReminderMessage(reminder);

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderText,
                keyboardService.getReminderDetailsKeyboard(securityService.getAuthenticatedUser().getId(), reminder)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED);
    }
}
