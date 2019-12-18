package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.RemindMessage;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.RemindMessageService;
import ru.gadjini.reminder.service.security.SecurityService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderMessageSender {

    private ReminderMessageBuilder reminderMessageBuilder;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private RemindMessageService remindMessageService;

    private SecurityService securityService;

    @Autowired
    public ReminderMessageSender(ReminderMessageBuilder reminderMessageBuilder,
                                 MessageService messageService,
                                 InlineKeyboardService inlineKeyboardService,
                                 RemindMessageService remindMessageService,
                                 SecurityService securityService) {
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.remindMessageService = remindMessageService;
        this.securityService = securityService;
    }

    public void sendRepeatReminderSkippedFromList(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder));
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder));
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_SKIPPED_ANSWER);
    }

    public void sendRepeatReminderSkipped(String queryId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder));
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder));
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
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder), null);
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder), null);
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

    public void sendRepeatReminderCompletedFromList(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder));
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder));
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

    public void sendReminderCompleted(String queryId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder),
                    null
            );
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder), null);
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder), null);
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

    public void sendReminderCompletedFromList(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder),
                    inlineKeyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder), null);
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder),
                    inlineKeyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_COMPLETE_ANSWER);
    }

    public void sendReminderCreated(Reminder reminder, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String messageForReceiver;

        if (reminder.isMySelf()) {
            messageForReceiver = reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId());
        } else {
            messageForReceiver = reminderMessageBuilder.getNewReminder(reminder, reminder.getReceiverId());
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId()),
                    replyKeyboardMarkup
            );
        }
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getReceiverReminderKeyboard(reminder.getId(), reminder.isRepeatable());
        int messageId = messageService.sendMessage(reminder.getReceiver().getChatId(), messageForReceiver, keyboard).getMessageId();

        remindMessageService.create(reminder.getId(), messageId);
    }

    public void sendReminderTimeChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = oldReminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(oldReminder.getId());
        }

        if (!oldReminder.isMySelf()) {
            String message = reminderMessageBuilder.getReminderTimeChanged(
                    oldReminder.getText(),
                    oldReminder.getCreator(),
                    oldReminder.getRemindAtInReceiverZone(),
                    updateReminderResult.getNewReminder().getRemindAtInReceiverZone()
            );
            messageService.sendMessage(oldReminder.getReceiver().getChatId(), message, null);
        }
        String newReminderText = reminderMessageBuilder.getReminderMessage(updateReminderResult.getNewReminder(), updateReminderResult.getNewReminder().getCreatorId());
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getEditReminderKeyboard(oldReminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME);
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboard);
    }

    public void sendReminderPostponed(UpdateReminderResult updateReminderResult, String reason, ReplyKeyboard replyKeyboard) {
        Reminder reminder = updateReminderResult.getOldReminder();
        RemindMessage remindMessage = reminder.getRemindMessage();

        if (remindMessage != null) {
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    remindMessage.getMessageId(),
                    reminderMessageBuilder.getReminderPostponedForReceiver(
                            reminder.getText(),
                            updateReminderResult.getNewReminder().getRemindAtInReceiverZone()
                    ),
                    inlineKeyboardService.getReceiverReminderKeyboard(reminder.getId(), reminder.isRepeatable())
            );
        }
        if (!reminder.isMySelf()) {
            String message = reminderMessageBuilder.getReminderPostponedForCreator(reminder.getText(), reminder.getReceiver(), updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason);
            messageService.sendMessage(reminder.getCreator().getChatId(), message);
        }
        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMINDER_POSTPONED, replyKeyboard);
    }

    public void sendReminderTextChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();

        RemindMessage remindMessage = oldReminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(oldReminder.getId());
        }

        if (!oldReminder.isMySelf()) {
            String message = reminderMessageBuilder.getReminderTextChanged(
                    oldReminder.getText(),
                    updateReminderResult.getNewReminder().getText(),
                    oldReminder.getCreator()
            );
            messageService.sendMessage(oldReminder.getReceiver().getChatId(), message);
        }
        String newReminderText = reminderMessageBuilder.getReminderMessage(updateReminderResult.getNewReminder());
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getEditReminderKeyboard(oldReminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME);
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
        messageService.editMessageByMessageCode(chatId, messageId, MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED, inlineKeyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME));
    }

    public void sendRepeatReminderStoppedFromList(String queryId, int messageId, Reminder reminder) {
        RemindMessage remindMessage = reminder.getRemindMessage();
        if (remindMessage != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), remindMessage.getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder));
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder));
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_STOPPED_ANSWER);
    }

    public void sendRepeatReminderStopped(String queryId, Reminder reminder) {
        if (reminder.getRemindMessage() != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder));
        } else {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder));
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder));
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
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getReminderDeletedForReceiver(reminder), null);
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_DELETED);
        messageService.editMessageByMessageCode(
                reminder.getCreator().getChatId(),
                messageId,
                reminderMessageBuilder.getReminderDeletedForCreator(reminder),
                inlineKeyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
        );
    }

    public void sendReminderCanceled(String queryId, Reminder reminder) {
        if (reminder.getRemindMessage() != null) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfReminderCanceled(reminder), null);
        } else {
            messageService.sendMessage(
                    reminder.getReceiver().getChatId(),
                    reminderMessageBuilder.getReminderCanceledForReceiver(reminder),
                    null
            );
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    reminderMessageBuilder.getReminderCanceledForCreator(reminder),
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
                    reminderMessageBuilder.getMySelfReminderCanceled(reminder),
                    inlineKeyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    reminderMessageBuilder.getReminderCanceledForReceiver(reminder),
                    inlineKeyboardService.goBackCallbackButton(MessagesProperties.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
            messageService.sendMessage(
                    reminder.getCreator().getChatId(),
                    reminderMessageBuilder.getReminderCanceledForCreator(reminder),
                    null
            );
        }
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_CANCELED_ANSWER);
    }

    public void sendCompletedReminders(long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageByMessageCode(
                    chatId,
                    messageId,
                    MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY,
                    inlineKeyboardService.getEmptyRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        } else {
            User user = securityService.getAuthenticatedUser();

            messageService.editMessage(
                    chatId,
                    messageId,
                    reminderMessageBuilder.getRemindersList(user.getId(), reminders),
                    inlineKeyboardService.getCompletedRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        }
    }

    public void sendActiveReminders(long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageByMessageCode(
                    chatId,
                    messageId,
                    MessagesProperties.MESSAGE_ACTIVE_REMINDERS_EMPTY,
                    inlineKeyboardService.getEmptyRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        } else {
            User user = securityService.getAuthenticatedUser();

            messageService.editMessage(
                    chatId,
                    messageId,
                    reminderMessageBuilder.getRemindersList(user.getId(), reminders),
                    inlineKeyboardService.getActiveRemindersListKeyboard(reminders.stream().map(Reminder::getId).collect(Collectors.toList()), MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        }
    }

    public void sendReminderEdit(Long chatId, Integer messageId, int reminderId) {
        messageService.editReplyKeyboard(
                chatId,
                messageId,
                inlineKeyboardService.getEditReminderKeyboard(reminderId, MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)
        );
    }

    public void sendReminderDetails(Long chatId, Integer messageId, Reminder reminder) {
        String text = reminderMessageBuilder.getReminderMessage(reminder);
        User user = securityService.getAuthenticatedUser();

        messageService.editMessage(
                chatId,
                messageId,
                text,
                inlineKeyboardService.getReminderDetailsKeyboard(user.getId(), reminder)
        );
    }

    public void sendCompletedRemindersDeleted(long chatId, int messageId) {
        messageService.editMessageByMessageCode(
                chatId,
                messageId,
                MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY,
                inlineKeyboardService.getEmptyRemindersListKeyboard(MessagesProperties.GET_REMINDERS_COMMAND_HISTORY_NAME)
        );
    }

    public void sendReminderNoteChanged(Reminder reminder, int messageId) {
        if (!reminder.isMySelf()) {
            String text = reminderMessageBuilder.getReminderNoteChangedForReceiver(reminder.getText(), reminder.getNote(), reminder.getCreator(), reminder.getRemindAtInReceiverZone());
            messageService.sendMessage(reminder.getReceiver().getChatId(), text, null);
        }

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderMessageBuilder.getReminderMessage(reminder),
                inlineKeyboardService.getEditReminderKeyboard(reminder.getId(), MessagesProperties.REMINDER_DETAILS_COMMAND_NAME)
        );
    }

    public void sendReminderNoteDeleted(String queryId, int messageId, Reminder reminder) {
        if (!reminder.isMySelf()) {
            String text = reminderMessageBuilder.getReminderNoteDeletedReceiver(reminder.getText(), reminder.getCreator(), reminder.getRemindAtInReceiverZone());
            messageService.sendMessage(reminder.getReceiver().getChatId(), text, null);
        }

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderMessageBuilder.getReminderMessage(reminder),
                inlineKeyboardService.getReminderDetailsKeyboard(securityService.getAuthenticatedUser().getId(), reminder)
        );
        messageService.sendAnswerCallbackQueryByMessageCode(queryId, MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED);
    }
}
