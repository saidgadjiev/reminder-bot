package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
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

    private LocalisationService localisationService;

    @Autowired
    public ReminderMessageSender(ReminderMessageBuilder reminderMessageBuilder,
                                 MessageService messageService,
                                 InlineKeyboardService inlineKeyboardService,
                                 RemindMessageService remindMessageService, LocalisationService localisationService) {
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.remindMessageService = remindMessageService;
        this.localisationService = localisationService;
    }

    public void sendRepeatReminderSkipped(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder))
            );
        }
    }

    public void sendRepeatReminderSkippedFromList(int messageId, int userId, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId()))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendRepeatReminderReturnedFromList(long chatId, int messageId, int userId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderReturnedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId()))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendRepeatReminderCantBeReturnedFromList(long chatId, int messageId, int userId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_RETURNED))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
    }

    public void sendReminderFullyUpdate(UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();

        if (oldReminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(oldReminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderEdited())
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(oldReminder.getId()))
            );
            if (oldReminder.hasRemindMessage()) {
                messageService.deleteMessage(oldReminder.getCreatorId(), oldReminder.getRemindMessage().getMessageId());
                remindMessageService.delete(oldReminder.getId());
            }
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(oldReminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderEdited())
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(oldReminder.getId()))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(oldReminder.getReceiverId())
                            .text(reminderMessageBuilder.getFullyUpdateMessageForReceiver(oldReminder, newReminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(oldReminder.getId()))
            );
            tryDeleteRemindMessage(-1, oldReminder);
        }
    }

    public void sendRepeatReminderCompleted(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId()))
            );
        }
    }

    public void sendRepeatReminderCompletedFromList(int messageId, int userId, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId()))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendReminderCompleted(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId()).text(reminderMessageBuilder.getMySelfReminderCompleted(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId()).text(reminderMessageBuilder.getReminderCompletedForReceiver(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId()).text(reminderMessageBuilder.getReminderCompletedForCreator(reminder))
            );
        }
    }

    public void sendReminderCompletedFromList(int messageId, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfReminderCompleted(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderCompletedForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderCompletedForCreator(reminder))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendReminderCreated(Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId()))
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder)),
                    message -> remindMessageService.create(reminder.getId(), message.getMessageId())
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getNewReminder(reminder, reminder.getReceiverId()))
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder)),
                    message -> remindMessageService.create(reminder.getId(), message.getMessageId())
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId()))
                            .replyKeyboard(inlineKeyboardService.getCreatorReminderKeyboard(reminder))
            );
        }
    }

    public void sendReminderTimeChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(oldReminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(newReminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(newReminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME))
        );
        if (newReminder.isMySelf()) {
            tryEditRemindMessage(messageId, newReminder, reminderMessageBuilder.getReminderMessage(newReminder, newReminder.getReceiverId()));
        } else {
            sendEditMessageToReceiver(newReminder, reminderMessageBuilder.getReminderTimeChanged(oldReminder, newReminder));
        }
    }

    public void sendReminderPostponed(int userId, int messageId, UpdateReminderResult updateReminderResult, String reason) {
        Reminder reminder = updateReminderResult.getOldReminder();

        if (reminder.isMySelf()) {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfReminderPostponed(reminder.getText(), reminder.getRemindAt()))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
            tryEditRemindMessage(messageId, updateReminderResult.getOldReminder(), reminderMessageBuilder.getMySelfReminderPostponed(reminder.getText(), reminder.getRemindAt()));
        } else {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderPostponedForReceiver(reminder.getText(), reminder.getReceiver(),
                                    updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getReminderPostponedForReceiver(reminder.getText(), reminder.getReceiver(),
                                    updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason))
            );
            tryEditRemindMessage(messageId, updateReminderResult.getOldReminder(), reminderMessageBuilder.getReminderPostponedForReceiver(reminder.getText(), reminder.getReceiver(),
                    updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason));
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderPostponedForCreator(reminder.getText(), reminder.getReceiver(),
                                    updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId()))
            );

        }
    }

    public void sendReminderTextChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(oldReminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(newReminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(newReminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME))
        );
        if (newReminder.isMySelf()) {
            tryEditRemindMessage(messageId, newReminder, reminderMessageBuilder.getReminderMessage(newReminder, newReminder.getReceiverId()));
        } else {
            sendEditMessageToReceiver(newReminder, reminderMessageBuilder.getReminderTextChanged(oldReminder.getText(), newReminder.getText(), newReminder.getCreator()));
        }
    }

    public void sendReminderNotFound(long chatId, int messageId) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH).chatId(chatId).text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOT_FOUND)));
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompleted(long chatId, int messageId) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH).chatId(chatId).text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED)));
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompletedFromList(long chatId, int messageId) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
        );
    }

    public void sendRepeatReminderStoppedFromList(int messageId, Reminder reminder) {
       if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME)));
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder))
            );
        }
       tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendRepeatReminderStopped(Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder))
            );
        }
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }
    }

    public void sendReminderDeleted(int messageId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderDeletedForCreator(reminder))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
        );
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getReminderDeletedForReceiver(reminder))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendReminderCanceled(Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getMySelfReminderCanceled(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getReminderCanceledForReceiver(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderCanceledForCreator(reminder))
            );
        }
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }
    }

    public void sendReminderCanceledFromList(int messageId, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfReminderCanceled(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderCanceledForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderCanceledForCreator(reminder))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendCompletedReminders(long chatId, int userId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY))
                            .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getCompletedRemindersList(userId, reminders))
                            .replyKeyboard(inlineKeyboardService.getCompletedRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
            );
        }
    }

    public void sendActiveReminders(int userId, long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_ACTIVE_REMINDERS_EMPTY))
                            .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getActiveRemindersList(userId, reminders))
                            .replyKeyboard(inlineKeyboardService.getActiveRemindersListKeyboard(reminders.stream().map(Reminder::getId).collect(Collectors.toList()), CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
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

    public void sendReminderDetails(long chatId, int userId, Integer messageId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
    }

    public void sendCompletedRemindersDeleted(long chatId, int messageId) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY))
                        .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
        );
    }

    public void sendReminderNoteChanged(Reminder reminder, int messageId) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME))
        );
        if (reminder.isMySelf()) {
            tryEditRemindMessage(messageId, reminder, reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()));
        } else {
            sendEditMessageToReceiver(reminder, reminderMessageBuilder.getReminderNoteChangedForReceiver(reminder.getText(), reminder.getNote(), reminder.getCreator()));
        }
    }

    public void sendReminderNoteDeleted(int messageId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME))
        );
        if (reminder.isMySelf()) {
            tryEditRemindMessage(messageId, reminder, reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()));
        } else {
            sendEditMessageToReceiver(reminder, reminderMessageBuilder.getReminderNoteDeletedReceiver(reminder.getCreator(), reminder.getText()));
        }
    }

    public void sendReminderDeactivated(int messageId, int userId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId()))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
        if (reminder.isMySelf()) {
            tryDeleteRemindMessage(messageId, reminder);
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getReminderDeactivatedReceiver(reminder))
            );
        }
    }

    public void sendReminderActivated(int messageId, int userId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId()))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getReminderActivatedReceiver(reminder))
            );
        }
    }

    public void sendCountSeriesEnabledOrDisabled(long chatId, int messageId, int userId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
    }

    public void sendReminderRead(long chatId, int messageId, Reminder reminder) {
        if (reminder.isNotMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()))
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder)));
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReadReminderCreator(reminder))
            );
        }
    }

    private void tryDeleteRemindMessage(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage() && messageId != reminder.getRemindMessage().getMessageId()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }
    }

    private void tryEditRemindMessage(int messageId, Reminder reminder, String text) {
        if (reminder.hasRemindMessage() && messageId != reminder.getRemindMessage().getMessageId()) {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .messageId(reminder.getRemindMessage().getMessageId())
                            .text(text)
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder))
            );
        }
    }

    private void sendEditMessageToReceiver(Reminder reminder, String text) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getRemindMessage().getMessageId());
        }
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(reminder.getReceiverId())
                        .text(text)
                        .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId())),
                message -> remindMessageService.create(reminder.getId(), message.getMessageId()));
    }
}
