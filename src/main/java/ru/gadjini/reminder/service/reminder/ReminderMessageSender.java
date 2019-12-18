package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public void sendRemindMessage(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
        }

        String message;
        if (reminder.isMySelf()) {
            message = messageBuilder.getRemindMySelf(reminder, itsTime, nextRemindAt);
        } else {
            message = messageBuilder.getRemindForReceiver(reminder, itsTime, nextRemindAt);
        }

        InlineKeyboardMarkup keyboard = keyboardService.getRemindKeyboard(reminder.getId(), itsTime, reminder.isRepeatable());
        int messageId = messageService.sendMessage(reminder.getReceiver().getChatId(), message, keyboard).getMessageId();
        remindMessageService.create(reminder.getId(), messageId);
    }

    public void sendRepeatReminderSkipped(String queryId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    messageBuilder.getMySelfRepeatReminderSkippedMessage(reminder),
                    null
            );
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), messageBuilder.getRepeatReminderSkippedMessageForCreator(reminder), null);
            messageService.sendMessage(reminder.getReceiver().getChatId(), messageBuilder.getRepeatReminderSkippedMessageForReceiver(reminder), null);
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_SKIPPED_ANSWER);
    }

    public void sendRepeatReminderCompleted(String queryId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    messageBuilder.getMySelfRepeatReminderCompletedMessage(reminder),
                    null
            );
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), messageBuilder.getRepeatReminderCompletedMessageForCreator(reminder), null);
            messageService.sendMessage(reminder.getReceiver().getChatId(), messageBuilder.getRepeatReminderCompletedMessageForReceiver(reminder), null);
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

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
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                    new Object[]{reminderText}
            );
        } else {
            StringBuilder messageForCreator = new StringBuilder();
            messageForCreator.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_COMPLETED, new Object[]{reminderText})).append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(reminder.getReceiver())}));
            messageService.sendMessage(reminder.getCreator().getChatId(), messageForCreator.toString(), null);

            StringBuilder messageForReceiver = new StringBuilder();
            messageForReceiver.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_COMPLETED, new Object[]{reminderText}))
                    .append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(reminder.getCreator())}));
            messageService.sendMessage(reminder.getReceiver().getChatId(), messageForReceiver.toString(), null);
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

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
                    MessagesProperties.MESSAGE_REMINDER_COMPLETED,
                    new Object[]{reminderText},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            StringBuilder messageForCreator = new StringBuilder();
            messageForCreator
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_COMPLETED, new Object[]{reminderText})).append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(reminder.getReceiver())}));
            messageService.sendMessage(reminder.getCreator().getChatId(), messageForCreator.toString(), null);

            StringBuilder message = new StringBuilder();
            message
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_COMPLETED, new Object[]{reminderText})).append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(reminder.getCreator())}));
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    message.toString(),
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
        InlineKeyboardMarkup keyboard = keyboardService.getReceiverReminderKeyboard(reminder.getId(), reminder.isRepeatable());
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
                    ),
                    keyboardService.getReceiverReminderKeyboard(reminder.getId(), reminder.isRepeatable())
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

    public void sendRepeatReminderStopped(String queryId, Reminder reminder) {
        if (reminder.getRemindMessage() != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageByCode(
                    reminder.getReceiver().getChatId(),
                    MessagesProperties.MESSAGE_REMINDER_STOPPED,
                    new Object[]{reminder.getText()}
            );
        } else {
            StringBuilder messageForReceiver = new StringBuilder();
            messageForReceiver
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_STOPPED, new Object[]{reminder.getText()}))
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(reminder.getCreator())}));
            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    messageForReceiver.toString(),
                    null
            );

            StringBuilder messageForCreator = new StringBuilder();
            messageForCreator
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_STOPPED, new Object[]{reminder.getText()}))
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(reminder.getReceiver())}));
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    messageForCreator.toString(),
                    null
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_STOPPED_ANSWER);
    }

    public void sendReminderDeleted(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (!reminder.isMySelf()) {
            StringBuilder messageForReceiver = new StringBuilder();
            messageForReceiver
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DELETED, new Object[]{reminder.getText()}))
                    .append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(reminder.getCreator())}));
            messageService.sendMessage(reminder.getReceiver().getChatId(), messageForReceiver.toString(), null);
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_DELETED);
        messageService.editMessageByMessageCode(
                reminder.getCreator().getChatId(),
                messageId,
                MessagesProperties.MESSAGE_REMINDER_DELETED,
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
                    MessagesProperties.MESSAGE_REMINDER_CANCELED,
                    new Object[]{reminder.getText()}
            );
        } else {
            StringBuilder messageForReceiver = new StringBuilder();
            messageForReceiver
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANCELED, new Object[]{reminder.getText()}))
                    .append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(reminder.getCreator())}));
            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    messageForReceiver.toString(),
                    null
            );

            StringBuilder messageForCreator = new StringBuilder();
            messageForCreator
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANCELED, new Object[]{reminder.getText()}))
                    .append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(reminder.getReceiver())}));
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    messageForCreator.toString(),
                    null
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
                    MessagesProperties.MESSAGE_REMINDER_CANCELED,
                    new Object[]{reminder.getText()},
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            StringBuilder messageForReceiver = new StringBuilder();
            messageForReceiver
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANCELED, new Object[]{reminder.getText()}))
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(reminder.getCreator())}));
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    messageForReceiver.toString(),
                    keyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );

            StringBuilder messageForCreator = new StringBuilder();
            messageForCreator
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANCELED, new Object[]{reminder.getText()}))
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(reminder.getReceiver())}));
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    messageForCreator.toString(),
                    null
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANCELED_ANSWER);
    }

    public void sendCustomRemindCreated(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = messageBuilder.getReminderMessage(customRemindResult.getReminderNotification().getReminder());

        messageService.editMessage(
                chatId,
                messageId,
                text + "\n\n" + messageBuilder.getCustomRemindText(customRemindResult),
                keyboardService.getReceiverReminderKeyboard(customRemindResult.getReminderNotification().getReminderId(), customRemindResult.getReminderNotification().getReminder().isRepeatable())
        );
    }

    public void sendCustomRemindCreatedFromReminderTimeDetails(long chatId, int messageId, CustomRemindResult customRemindResult, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String text = messageBuilder.getReminderTimeMessage(customRemindResult.getReminderNotification());

        messageService.editMessage(
                chatId,
                messageId,
                text,
                keyboardService.getReminderTimeKeyboard(customRemindResult.getReminderNotification().getId(), customRemindResult.getReminderNotification().getReminderId())
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

            String text = messageBuilder.getRemindersListMessage(user.getId(), reminders);

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

            String text = messageBuilder.getRemindersListMessage(user.getId(), reminders);

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
