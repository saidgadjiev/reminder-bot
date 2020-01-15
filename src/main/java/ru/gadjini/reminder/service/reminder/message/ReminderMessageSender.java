package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
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

    public void sendRepeatReminderSkippedFromList(int messageId, int userId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder))
            );
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
        }
    }

    public void sendRepeatReminderCantBeReturned(long chatId, int messageId, int userId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_RETURNED))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
    }

    public void sendRepeatReminderReturned(long chatId, int messageId, int userId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderReturnedForReceiver(reminder))
            );
        }
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
    }

    public void sendRepeatReminderSkipped(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder))
            );
        }
    }

    public void sendRepeatReminderCompleted(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder))
            );
        }
    }

    public void sendReminderFullyUpdate(UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();
        String receiverMessage;
        if (oldReminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(oldReminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getReminderEdited())
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(oldReminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getFullyUpdateMessageForReceiver(oldReminder, newReminder))
            );
            receiverMessage = reminderMessageBuilder.getReminderMessage(newReminder, oldReminder.getReceiverId());
        } else {
            receiverMessage = reminderMessageBuilder.getReminderMessage(newReminder);
        }
        if (oldReminder.hasRemindMessage()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(oldReminder.getReceiver().getChatId())
                            .messageId(oldReminder.getRemindMessage().getMessageId())
                            .text(receiverMessage)
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(newReminder))
            );
        }
    }

    public void sendRepeatReminderCompletedFromList(int messageId, int userId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        InlineKeyboardMarkup keyboardMarkup;
        if (reminder.isRepeatable()) {
            keyboardMarkup = inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder);
        } else {
            keyboardMarkup = inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME);
        }

        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder))
                            .replyKeyboard(keyboardMarkup)
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder))
            );
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder))
                            .replyKeyboard(keyboardMarkup)
            );
        }
    }

    public void sendReminderCompleted(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId()).text(reminderMessageBuilder.getMySelfReminderCompleted(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId()).text(reminderMessageBuilder.getReminderCompletedForCreator(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId()).text(reminderMessageBuilder.getReminderCompletedForReceiver(reminder))
            );
        }
    }

    public void sendReminderCompletedFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getCreator().getChatId()).text(reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder))
            );
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
        }
    }

    public void sendReminderCreated(Reminder reminder, ReplyKeyboardMarkup replyKeyboardMarkup) {
        String messageForReceiver;

        if (reminder.isMySelf()) {
            messageForReceiver = reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId());
        } else {
            messageForReceiver = reminderMessageBuilder.getNewReminder(reminder, reminder.getReceiverId());
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId()))
                            .replyKeyboard(inlineKeyboardService.getCreatorReminderKeyboard(reminder))
            );
        }
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getReceiverReminderKeyboard(reminder);
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getReceiver().getChatId()).text(messageForReceiver).replyKeyboard(keyboard),
                message -> remindMessageService.create(reminder.getId(), message.getMessageId())
        );
    }

    public void sendReminderTimeChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();

        editRemindMessage(messageId, newReminder, reminderMessageBuilder.getReminderMessage(newReminder, newReminder.getReceiverId()));

        if (oldReminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(oldReminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getReminderTimeChanged(oldReminder, newReminder))
            );
        }
        String newReminderText = reminderMessageBuilder.getReminderMessage(updateReminderResult.getNewReminder(), updateReminderResult.getNewReminder().getCreatorId());
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getEditReminderKeyboard(oldReminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME);
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(oldReminder.getCreator().getChatId()).messageId(messageId).text(newReminderText).replyKeyboard(keyboard)
        );
    }

    public void sendReminderPostponed(int userId, int messageId, UpdateReminderResult updateReminderResult, String reason, ReplyKeyboard replyKeyboard) {
        Reminder reminder = updateReminderResult.getOldReminder();

        String postponeMessage = reminderMessageBuilder.getReminderPostponedForReceiver(
                reminder.getText(),
                updateReminderResult.getNewReminder().getRemindAtInReceiverZone(),
                reason
        );
        editRemindMessage(messageId, updateReminderResult.getOldReminder(), postponeMessage);
        if (reminder.isNotMySelf()) {
            String message = reminderMessageBuilder.getReminderPostponedForCreator(reminder.getText(), reminder.getReceiver(), updateReminderResult.getNewReminder().getRemindAtInReceiverZone(), reason);
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(message)
            );
        }
        messageService.editMessage(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(reminder.getReceiver().getChatId())
                        .messageId(messageId)
                        .text(postponeMessage)
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(reminder.getReceiver().getChatId())
                        .text(postponeMessage)
                        .replyKeyboard(replyKeyboard)
        );
    }

    public void sendReminderTextChanged(int messageId, UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();

        editRemindMessage(messageId, newReminder, reminderMessageBuilder.getReminderMessage(newReminder, oldReminder.getReceiverId()));

        if (oldReminder.isNotMySelf()) {
            String message = reminderMessageBuilder.getReminderTextChanged(
                    oldReminder.getText(),
                    updateReminderResult.getNewReminder().getText(),
                    oldReminder.getCreator()
            );
            messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(oldReminder.getReceiver().getChatId()).text(message));
        }
        String newReminderText = reminderMessageBuilder.getReminderMessage(updateReminderResult.getNewReminder());
        InlineKeyboardMarkup keyboard = inlineKeyboardService.getEditReminderKeyboard(oldReminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME);
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(oldReminder.getCreator().getChatId()).messageId(messageId).text(newReminderText).replyKeyboard(keyboard)
        );
    }

    public void sendReminderNotFound(long chatId, int messageId) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(chatId).text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOT_FOUND)));
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompleted(long chatId, int messageId) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(chatId).text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED)));
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompletedFromList(long chatId, int messageId) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
        );
    }

    public void sendRepeatReminderStoppedFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder))
            );
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder)));
        }
    }

    public void sendRepeatReminderStopped(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder))
            );
        }
    }

    public void sendReminderDeleted(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getReminderDeletedForReceiver(reminder))
            );
        }
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(reminder.getCreator().getChatId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderDeletedForCreator(reminder))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
        );
    }

    public void sendReminderCanceled(Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getReceiver().getChatId()).text(reminderMessageBuilder.getMySelfReminderCanceled(reminder))
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getReceiver().getChatId()).text(reminderMessageBuilder.getReminderCanceledForReceiver(reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getCreator().getChatId()).text(reminderMessageBuilder.getReminderCanceledForCreator(reminder))
            );
        }
    }

    public void sendReminderCanceledFromList(int messageId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfReminderCanceled(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderCanceledForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getCreator().getChatId()).text(reminderMessageBuilder.getReminderCanceledForCreator(reminder))
            );
        }
    }

    public void sendCompletedReminders(int userId, long chatId, int messageId, List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY))
                            .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
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
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_ACTIVE_REMINDERS_EMPTY))
                            .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
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

    public void sendReminderDetails(int userId, Long chatId, Integer messageId, Reminder reminder) {
        String text = reminderMessageBuilder.getReminderMessage(reminder);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(text)
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
    }

    public void sendCompletedRemindersDeleted(long chatId, int messageId) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY))
                        .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME))
        );
    }

    public void sendReminderNoteChanged(Reminder reminder, int messageId) {
        editRemindMessage(messageId, reminder, reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()));

        if (reminder.isNotMySelf()) {
            String text = reminderMessageBuilder.getReminderNoteChangedForReceiver(reminder.getText(), reminder.getNote(), reminder.getCreator());
            messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getReceiver().getChatId()).text(text));
        }

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(reminder.getCreator().getChatId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME))
        );
    }

    public void sendReminderNoteDeleted(int messageId, Reminder reminder) {
        editRemindMessage(messageId, reminder, reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()));

        if (reminder.isNotMySelf()) {
            String text = reminderMessageBuilder.getReminderNoteDeletedReceiver(reminder.getCreator(), reminder.getText());
            messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.MEDIUM).chatId(reminder.getReceiver().getChatId()).text(text));
        }

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(reminder.getCreator().getChatId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(reminder.getId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME))
        );
    }

    public void sendReminderDeactivated(long chatId, int messageId, int userId, Reminder reminder) {
        if (reminder.hasRemindMessage()) {
            messageService.deleteMessage(reminder.getReceiver().getChatId(), reminder.getRemindMessage().getMessageId());
            remindMessageService.delete(reminder.getId());
        }

        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderMessage(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId()))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getReminderDeactivatedReceiver(reminder))
            );
        }
    }

    public void sendReminderActivated(long chatId, int messageId, int userId, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderMessage(reminder))
                            .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
        } else {
            messageService.editMessageAsync(new EditMessageContext(PriorityJob.Priority.MEDIUM)
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId()))
                    .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .text(reminderMessageBuilder.getReminderActivatedReceiver(reminder))
            );
        }
    }

    public void sendCountSeriesEnabledOrDisabled(long chatId, int messageId, int userId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, reminder))
        );
    }

    public void sendReminderRead(long chatId, int messageId, Reminder reminder) {
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreator().getChatId())
                            .text(reminderMessageBuilder.getReadReminderCreator(reminder))
            );
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()))
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder)));
        }
    }

    private void editRemindMessage(int messageId, Reminder reminder, String text) {
        if (reminder.hasRemindMessage() && messageId != reminder.getRemindMessage().getMessageId()) {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiver().getChatId())
                            .messageId(reminder.getRemindMessage().getMessageId())
                            .text(text)
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder))
            );
        }
    }
}
