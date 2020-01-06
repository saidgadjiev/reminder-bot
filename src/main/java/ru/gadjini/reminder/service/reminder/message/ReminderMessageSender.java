package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.RemindMessageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderMessageSender {

    private ReminderMessageBuilder reminderMessageBuilder;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private RemindMessageService remindMessageService;

    @Autowired
    public ReminderMessageSender(ReminderMessageBuilder reminderMessageBuilder,
                                 MessageService messageService,
                                 InlineKeyboardService inlineKeyboardService,
                                 RemindMessageService remindMessageService) {
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.remindMessageService = remindMessageService;
    }

    public void sendRepeatReminderSkippedFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder));
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder));
        }
    }

    public void sendRepeatReminderSkipped(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder));
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder));
        }
    }

    public void sendRepeatReminderCompleted(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder));
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder));
        }
    }

    public void sendReminderFullyUpdate(UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();
        String receiverMessage;
        if (oldReminder.isNotMySelf()) {
            messageService.sendMessage(oldReminder.getCreator().getChatId(), reminderMessageBuilder.getReminderEdited());
            messageService.sendMessage(oldReminder.getReceiver().getChatId(), reminderMessageBuilder.getFullyUpdateMessageForReceiver(oldReminder, newReminder));
            receiverMessage = reminderMessageBuilder.getReminderMessage(newReminder, oldReminder.getReceiverId());
        } else {
            receiverMessage = reminderMessageBuilder.getReminderMessage(newReminder);
        }
        messageService.editMessage(
                oldReminder.getReceiver().getChatId(),
                oldReminder.getRemindMessage().getMessageId(),
                receiverMessage,
                inlineKeyboardService.getReceiverReminderKeyboard(oldReminder.getId(), newReminder.isRepeatable(), newReminder.getRemindAt().hasTime())
        );
    }

    public void sendRepeatReminderCompletedFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder));
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder));
        }
    }

    public void sendReminderCompleted(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfReminderCompleted(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getReminderCompletedForCreator(reminder));
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getReminderCompletedForReceiver(reminder));
        }
    }

    public void sendReminderCompletedFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageByMessageCode(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder),
                    inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder));
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder),
                    inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        }
    }

    public void sendReminderCreated(Reminder reminder, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String messageForReceiver;

        if (reminder.isMySelf()) {
            messageForReceiver = reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId());
        } else {
            messageForReceiver = reminderMessageBuilder.getNewReminder(reminder, reminder.getReceiverId());
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId()), replyKeyboardMarkup);
        }
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getReceiverReminderKeyboard(reminder.getId(), reminder.isRepeatable(), reminder.getRemindAt().hasTime());
        messageService.sendMessage(reminder.getReceiver().getChatId(), messageForReceiver, keyboard, message -> remindMessageService.create(reminder.getId(), message.getMessageId()));
    }

    public void sendReminderTimeChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        if (oldReminder.hasRemindMessage()) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), oldReminder.getRemindMessage().getMessageId());
            remindMessageService.delete(oldReminder.getId());
        }

        Reminder newReminder = updateReminderResult.getNewReminder();
        if (oldReminder.isNotMySelf()) {
            messageService.sendMessage(oldReminder.getReceiver().getChatId(), reminderMessageBuilder.getReminderTimeChanged(oldReminder, newReminder));
        }
        String newReminderText = reminderMessageBuilder.getReminderMessage(updateReminderResult.getNewReminder(), updateReminderResult.getNewReminder().getCreatorId());
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getEditReminderKeyboard(oldReminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME);
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboard);
    }

    public void sendReminderPostponed(UpdateReminderResult updateReminderResult, String reason, ReplyKeyboard replyKeyboard) {
        Reminder reminder = updateReminderResult.getOldReminder();

        if (reminder.hasRemindMessage()) {
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    reminder.getRemindMessage().getMessageId(),
                    reminderMessageBuilder.getReminderPostponedForReceiver(
                            reminder.getText(),
                            updateReminderResult.getNewReminder().getRemindAtInReceiverZone(),
                            null
                    ),
                    inlineKeyboardService.getReceiverReminderKeyboard(reminder.getId(), reminder.isRepeatable(), reminder.getRemindAt().hasTime())
            );
        }
        if (reminder.isNotMySelf()) {
            String message = reminderMessageBuilder.getReminderPostponedForCreator(reminder.getText(), reminder.getReceiver(), updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason);
            messageService.sendMessage(reminder.getCreator().getChatId(), message);
        }
        messageService.sendMessage(
                reminder.getReceiver().getChatId(),
                reminderMessageBuilder.getReminderPostponedForReceiver(reminder.getText(), updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason),
                replyKeyboard
        );
    }

    public void sendReminderTextChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();

        if (oldReminder.hasRemindMessage()) {
            messageService.deleteMessage(oldReminder.getReceiver().getChatId(), oldReminder.getRemindMessage().getMessageId());
            remindMessageService.delete(oldReminder.getId());
        }

        if (oldReminder.isNotMySelf()) {
            String message = reminderMessageBuilder.getReminderTextChanged(
                    oldReminder.getText(),
                    updateReminderResult.getNewReminder().getText(),
                    oldReminder.getCreator()
            );
            messageService.sendMessage(oldReminder.getReceiver().getChatId(), message);
        }
        String newReminderText = reminderMessageBuilder.getReminderMessage(updateReminderResult.getNewReminder());
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getEditReminderKeyboard(oldReminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME);
        messageService.editMessage(oldReminder.getCreator().getChatId(), messageId, newReminderText, keyboard);
    }

    public void sendReminderNotFound(long chatId, int messageId) {
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_REMINDER_NOT_FOUND);
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompleted(long chatId, int messageId) {
        messageService.sendMessageByCode(chatId, MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED);
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompletedFromList(long chatId, int messageId) {
        messageService.editMessageByMessageCode(chatId, messageId, MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED, inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME));
    }

    public void sendRepeatReminderStoppedFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder));
        } else {
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder));
            messageService.editMessage(reminder.getReceiver().getChatId(), messageId, reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder));
        }
    }

    public void sendRepeatReminderStopped(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder));
        } else {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder));
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder));
        }
    }

    public void sendReminderDeleted(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isNotMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getReminderDeletedForReceiver(reminder));
        }
        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderMessageBuilder.getReminderDeletedForCreator(reminder),
                inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)
        );
    }

    public void sendReminderCanceled(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getMySelfReminderCanceled(reminder));
        } else {
            messageService.sendMessage(reminder.getReceiver().getChatId(), reminderMessageBuilder.getReminderCanceledForReceiver(reminder));
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getReminderCanceledForCreator(reminder));
        }
    }

    public void sendReminderCanceledFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    reminderMessageBuilder.getMySelfReminderCanceled(reminder),
                    inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
        } else {
            messageService.editMessage(
                    reminder.getReceiver().getChatId(),
                    messageId,
                    reminderMessageBuilder.getReminderCanceledForReceiver(reminder),
                    inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)
            );
            messageService.sendMessage(reminder.getCreator().getChatId(), reminderMessageBuilder.getReminderCanceledForCreator(reminder));
        }
    }

    public void sendCompletedReminders(int userId, long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageByMessageCode(
                    chatId,
                    messageId,
                    MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY,
                    inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        } else {
            messageService.editMessage(
                    chatId,
                    messageId,
                    reminderMessageBuilder.getCompletedRemindersList(userId, reminders),
                    inlineKeyboardService.getCompletedRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        }
    }

    public void sendActiveReminders(int userId, long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageByMessageCode(
                    chatId,
                    messageId,
                    MessagesProperties.MESSAGE_ACTIVE_REMINDERS_EMPTY,
                    inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        } else {
            messageService.editMessage(
                    chatId,
                    messageId,
                    reminderMessageBuilder.getActiveRemindersList(userId, reminders),
                    inlineKeyboardService.getActiveRemindersListKeyboard(reminders.stream().map(Reminder::getId).collect(Collectors.toList()), CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME)
            );
        }
    }

    public void sendReminderEdit(Long chatId, Integer messageId, int reminderId) {
        messageService.editReplyKeyboard(
                chatId,
                messageId,
                inlineKeyboardService.getEditReminderKeyboard(reminderId, CommandNames.REMINDER_DETAILS_COMMAND_NAME)
        );
    }

    public void sendReminderDetails(int userId, Long chatId, Integer messageId, Reminder reminder) {
        String text = reminderMessageBuilder.getReminderMessage(reminder);

        messageService.editMessage(
                chatId,
                messageId,
                text,
                inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder)
        );
    }

    public void sendCompletedRemindersDeleted(long chatId, int messageId) {
        messageService.editMessageByMessageCode(
                chatId,
                messageId,
                MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY,
                inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME)
        );
    }

    public void sendReminderNoteChanged(Reminder reminder, int messageId) {
        if (reminder.isNotMySelf()) {
            String text = reminderMessageBuilder.getReminderNoteChangedForReceiver(reminder.getText(), reminder.getNote(), reminder.getCreator());
            messageService.sendMessage(reminder.getReceiver().getChatId(), text);
        }

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderMessageBuilder.getReminderMessage(reminder),
                inlineKeyboardService.getEditReminderKeyboard(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME)
        );
    }

    public void sendReminderNoteDeleted(int messageId, Reminder reminder) {
        if (reminder.isNotMySelf()) {
            String text = reminderMessageBuilder.getReminderNoteDeletedReceiver(reminder.getCreator(), reminder.getText());
            messageService.sendMessage(reminder.getReceiver().getChatId(), text);
        }

        messageService.editMessage(
                reminder.getCreator().getChatId(),
                messageId,
                reminderMessageBuilder.getReminderMessage(reminder),
                inlineKeyboardService.getEditReminderKeyboard(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME)
        );
    }
}
