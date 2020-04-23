package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.EditMessageContext;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.UpdateReminderResult;
import ru.gadjini.reminder.request.RequestParams;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.util.KeyboardCustomizer;
import ru.gadjini.reminder.util.TextUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReminderMessageSender {

    private ReminderMessageBuilder reminderMessageBuilder;

    private MessageService messageService;

    private InlineKeyboardService inlineKeyboardService;

    private LocalisationService localisationService;

    private ReminderService reminderService;

    private TgUserService userService;

    @Autowired
    public ReminderMessageSender(ReminderMessageBuilder reminderMessageBuilder,
                                 MessageService messageService,
                                 InlineKeyboardService inlineKeyboardService,
                                 LocalisationService localisationService, ReminderService reminderService, TgUserService userService) {
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.localisationService = localisationService;
        this.reminderService = reminderService;
        this.userService = userService;
    }

    public void sendRepeatReminderSkipped(Reminder reminder) {
        if (reminder.hasReceiverMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
            reminderService.deleteReceiverMessage(reminder.getId());
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

    public void sendRepeatReminderSkippedFromList(int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderSkipped(reminder))
                            .replyKeyboard(inlineKeyboardMarkup)
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardMarkup)
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderSkippedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId(), reminder.getCreator().getLocale()))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendRepeatReminderReturnedFromList(long chatId, int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardMarkup)
        );
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderReturnedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId(), reminder.getCreator().getLocale()))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendRepeatReminderCantBeReturnedFromList(long chatId, int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Locale locale) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_RETURNED, locale))
                        .replyKeyboard(inlineKeyboardMarkup)
        );
    }

    public void sendReminderFullyUpdate(UpdateReminderResult updateReminderResult) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();

        if (oldReminder.isMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(oldReminder.getCreatorId())
                            .text(reminderMessageBuilder.getMySelfReminderEdited(newReminder))
            );
            if (oldReminder.hasReceiverMessage()) {
                messageService.editMessage(
                        new EditMessageContext(PriorityJob.Priority.MEDIUM)
                                .chatId(oldReminder.getReceiverId())
                                .messageId(oldReminder.getReceiverMessageId())
                                .text(reminderMessageBuilder.getReminderMessage(newReminder))
                                .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(newReminder))
                );
            }
        } else {
            if (oldReminder.getCreatorMessageId() != null) {
                messageService.deleteMessage(oldReminder.getCreatorId(), oldReminder.getCreatorMessageId());
                reminderService.deleteCreatorMessage(oldReminder.getId());
            }
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(oldReminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderEditedCreator(newReminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(oldReminder.getId(), oldReminder.getCreator().getLocale()))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(oldReminder.getReceiverId())
                            .text(reminderMessageBuilder.getFullyUpdateMessageForReceiver(oldReminder, newReminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(oldReminder.getId(), oldReminder.getReceiver().getLocale())),
                    message -> reminderService.setReceiverMessage(oldReminder.getId(), message.getMessageId())
            );
            tryDeleteRemindMessage(-1, oldReminder);
        }
    }

    public void sendRepeatReminderCompleted(Reminder reminder) {
        if (reminder.hasReceiverMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
            reminderService.deleteReceiverMessage(reminder.getId());
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
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId(), reminder.getCreator().getLocale()))
            );
        }
    }

    public void sendRepeatReminderCompletedFromList(int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderCompleted(reminder))
                            .replyKeyboard(inlineKeyboardMarkup)
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForReceiver(reminder))
                            .replyKeyboard(inlineKeyboardMarkup)
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderCompletedForCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId(), reminder.getCreator().getLocale()))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendReminderCompleted(Reminder reminder) {
        if (reminder.hasReceiverMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
            reminderService.deleteReceiverMessage(reminder.getId());
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

    public void sendReminderCompletedFromList(int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfReminderCompleted(reminder))
                            .replyKeyboard(new KeyboardCustomizer(inlineKeyboardMarkup).removeExclude(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME).getKeyboardMarkup())
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderCompletedForReceiver(reminder))
                            .replyKeyboard(new KeyboardCustomizer(inlineKeyboardMarkup).removeExclude(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME).getKeyboardMarkup())
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
                    message -> reminderService.setReceiverMessage(reminder.getId(), message.getMessageId())
            );
        } else {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getNewReminder(reminder, reminder.getCreatorId()))
                            .replyKeyboard(inlineKeyboardService.getCreatorReminderKeyboard(reminder)),
                    message -> reminderService.setCreatorMessage(reminder.getId(), message.getMessageId())
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getNewReminder(reminder, reminder.getReceiverId()))
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder)),
                    message -> reminderService.setReceiverMessage(reminder.getId(), message.getMessageId())
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
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(oldReminder.getId(), oldReminder.getChallengeId(),
                                CommandNames.REMINDER_DETAILS_COMMAND_NAME, oldReminder.getCreator().getLocale()))
        );
        if (oldReminder.isMySelf()) {
            tryEditRemindMessage(messageId, oldReminder, reminderMessageBuilder.getReminderMessage(newReminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(newReminder.getReceiverId())));
        } else {
            sendEditMessageToReceiver(oldReminder, reminderMessageBuilder.getReminderTimeChanged(oldReminder, newReminder));
        }
    }

    public void sendReminderPostponed(int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, UpdateReminderResult updateReminderResult, String reason) {
        Reminder oldReminder = updateReminderResult.getOldReminder();
        Reminder newReminder = updateReminderResult.getNewReminder();

        if (oldReminder.isMySelf()) {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(oldReminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfReminderPostponed(oldReminder.getText(), newReminder.getRemindAtInReceiverZone(), oldReminder.getReceiver().getLocale()))
                            .replyKeyboard(inlineKeyboardMarkup)
            );
            tryEditRemindMessage(messageId, updateReminderResult.getOldReminder(), reminderMessageBuilder.getMySelfReminderPostponed(oldReminder.getText(), newReminder.getRemindAtInReceiverZone(), oldReminder.getReceiver().getLocale()));
        } else {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(oldReminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderPostponedForReceiver(oldReminder.getText(), oldReminder.getCreator(), newReminder.getRemindAtInReceiverZone(), reason, oldReminder.getReceiver().getLocale()))
                            .replyKeyboard(inlineKeyboardMarkup)
            );
            tryEditRemindMessage(messageId, updateReminderResult.getOldReminder(), reminderMessageBuilder.getReminderPostponedForReceiver(oldReminder.getText(), oldReminder.getReceiver(),
                    newReminder.getRemindAtInReceiverZone(), reason, oldReminder.getReceiver().getLocale()));
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(oldReminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderPostponedForCreator(oldReminder.getText(), oldReminder.getReceiver(),
                                    newReminder.getRemindAtInReceiverZone(), reason, oldReminder.getCreator().getLocale()))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(oldReminder.getId(), oldReminder.getCreator().getLocale()))
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
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(newReminder.getId(),
                                oldReminder.getChallengeId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, oldReminder.getCreator().getLocale()))
        );
        if (newReminder.isMySelf()) {
            tryEditRemindMessage(messageId, newReminder, reminderMessageBuilder.getReminderMessage(newReminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(newReminder.getReceiverId())));
        } else {
            sendEditMessageToReceiver(newReminder, reminderMessageBuilder.getReminderTextChanged(oldReminder.getText(), newReminder.getText(), newReminder.getCreator(), oldReminder.getReceiver().getLocale()));
        }
    }

    public void sendReminderNotFound(long chatId, int messageId, Locale locale) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH).chatId(chatId).text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOT_FOUND, locale)));
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompleted(long chatId, int messageId, Locale locale) {
        messageService.sendMessageAsync(new SendMessageContext(PriorityJob.Priority.HIGH).chatId(chatId).text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED, locale)));
        messageService.deleteMessage(chatId, messageId);
    }

    public void sendReminderCantBeCompletedFromList(int userId, int messageId) {
        Locale locale = userService.getLocale(userId);

        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(userId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANT_BE_COMPLETED, locale))
                        .replyKeyboard(inlineKeyboardService.goBackCallbackButton(CommandNames.GET_ACTIVE_REMINDERS_COMMAND_NAME, locale))
        );
    }

    public void sendRepeatReminderStoppedFromList(int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfRepeatReminderStopped(reminder))
                            .replyKeyboard(new KeyboardCustomizer(inlineKeyboardMarkup).removeExclude(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME).getKeyboardMarkup())
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForReceiver(reminder))
                            .replyKeyboard(new KeyboardCustomizer(inlineKeyboardMarkup).removeExclude(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME).getKeyboardMarkup()));
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getRepeatReminderStoppedForCreator(reminder))
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
        if (reminder.hasReceiverMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
            reminderService.deleteReceiverMessage(reminder.getId());
        }
    }

    public void sendReminderDeleted(int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderDeletedForCreator(reminder))
                        .replyKeyboard(new KeyboardCustomizer(inlineKeyboardMarkup).removeExclude(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME).getKeyboardMarkup())
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

    public void sendReminderCanceled(Reminder reminder, String reason) {
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
                            .text(reminderMessageBuilder.getReminderCanceledForReceiver(reminder, reason))
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderCanceledForCreator(reminder, reason))
            );
        }
        if (reminder.hasReceiverMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
            reminderService.deleteReceiverMessage(reminder.getId());
        }
    }

    public void sendReminderCanceledFromList(int messageId, InlineKeyboardMarkup keyboardMarkup, Reminder reminder, String reason) {
        if (reminder.isMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getMySelfReminderCanceled(reminder))
                            .replyKeyboard(new KeyboardCustomizer(keyboardMarkup).removeExclude(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME).getKeyboardMarkup())
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(reminder.getReceiverId())
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderCanceledForReceiver(reminder, reason))
                            .replyKeyboard(new KeyboardCustomizer(keyboardMarkup).removeExclude(CommandNames.GO_BACK_CALLBACK_COMMAND_NAME).getKeyboardMarkup())
            );
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReminderCanceledForCreator(reminder, reason))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendCompletedReminders(long chatId, int userId, int messageId, List<Reminder> reminders) {
        Locale locale = userService.getLocale(userId);
        if (reminders.isEmpty()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY, locale))
                            .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME, locale))
            );
        } else {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getCompletedRemindersList(userId, reminders, locale))
                            .replyKeyboard(inlineKeyboardService.getCompletedRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME, locale))
            );
        }
    }

    public void sendActiveReminders(int userId, long chatId, int messageId, String currText, String header, RequestParams requestParams, List<Reminder> reminders) {
        Locale locale = userService.getLocale(userId);
        String text = reminderMessageBuilder.getActiveRemindersList(userId, reminders, header, locale);
        if (!Objects.equals(TextUtils.removeHtmlTags(text), currText)) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(text)
                            .replyKeyboard(
                                    reminders.isEmpty()
                                            ? inlineKeyboardService.getEmptyActiveRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME, locale)
                                            : inlineKeyboardService.getActiveRemindersListKeyboard(
                                            reminders.stream().map(Reminder::getId).collect(Collectors.toList()),
                                            CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME,
                                            requestParams,
                                            locale)
                            )
            );
        }
    }

    public void sendReminderEdit(int userId, Integer messageId, int reminderId, Integer challengeId) {
        messageService.editReplyKeyboard(
                userId,
                messageId,
                inlineKeyboardService.getEditReminderKeyboard(reminderId, challengeId, CommandNames.REMINDER_DETAILS_COMMAND_NAME, userService.getLocale(userId))
        );
    }

    public void sendReminderDetails(long chatId, int userId, Integer messageId, RequestParams requestParams, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(userId)))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, requestParams, reminder))
        );
    }

    public void sendCompletedRemindersDeleted(long chatId, int messageId, Locale locale) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_REMINDERS_EMPTY, locale))
                        .replyKeyboard(inlineKeyboardService.getEmptyRemindersListKeyboard(CommandNames.GET_REMINDERS_COMMAND_HISTORY_NAME, null))
        );
    }

    public void sendReminderNoteChanged(Reminder reminder, int messageId) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(reminder.getId(),
                                reminder.getChallengeId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, reminder.getCreator().getLocale()))
        );
        if (reminder.isMySelf()) {
            tryEditRemindMessage(messageId, reminder, reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getReceiverId())));
        } else {
            sendEditMessageToReceiver(reminder, reminderMessageBuilder.getReminderNoteChangedForReceiver(reminder.getText(), reminder.getNote(), reminder.getCreator(), reminder.getReceiver().getLocale()));
        }
    }

    public void sendReminderNoteDeleted(int messageId, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(inlineKeyboardService.getEditReminderKeyboard(reminder.getId(),
                                reminder.getChallengeId(), CommandNames.REMINDER_DETAILS_COMMAND_NAME, reminder.getCreator().getLocale()))
        );
        if (reminder.isMySelf()) {
            tryEditRemindMessage(messageId, reminder, reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getReceiverId())));
        } else {
            sendEditMessageToReceiver(reminder, reminderMessageBuilder.getReminderNoteDeletedReceiver(reminder));
        }
    }

    public void sendReminderDeactivated(int messageId, int userId, RequestParams requestParams, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getCreatorId())))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, requestParams, reminder))
        );
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getReminderDeactivatedReceiver(reminder))
            );
        }
        tryDeleteRemindMessage(messageId, reminder);
    }

    public void sendReminderActivated(int messageId, int userId, RequestParams requestParams, Reminder reminder) {
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(reminder.getCreatorId())
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getCreatorId())))
                        .replyKeyboard(inlineKeyboardService.getReminderDetailsKeyboard(userId, requestParams, reminder))
        );
        if (reminder.isNotMySelf()) {
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .text(reminderMessageBuilder.getReminderActivatedReceiver(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId(), reminder.getReceiver().getLocale())),
                    message -> reminderService.setReceiverMessage(reminder.getId(), message.getMessageId())
            );
        }
    }

    public void sendCountSeriesEnabledOrDisabled(long chatId, int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        InlineKeyboardMarkup newKeyboard;

        if (reminder.isCountSeries()) {
            newKeyboard = new KeyboardCustomizer(inlineKeyboardMarkup).replaceButton(
                    CommandNames.ENABLE_COUNT_SERIES_COMMAND_NAME,
                    CommandNames.DISABLE_COUNT_SERIES_COMMAND_NAME,
                    localisationService.getMessage(MessagesProperties.DISABLE_COUNT_SERIES_COMMAND_DESCRIPTION, reminder.getCreator().getLocale())
            ).getKeyboardMarkup();
        } else {
            newKeyboard = new KeyboardCustomizer(inlineKeyboardMarkup).replaceButton(
                    CommandNames.DISABLE_COUNT_SERIES_COMMAND_NAME,
                    CommandNames.ENABLE_COUNT_SERIES_COMMAND_NAME,
                    localisationService.getMessage(MessagesProperties.ENABLE_COUNT_SERIES_COMMAND_DESCRIPTION, reminder.getCreator().getLocale())
            ).getKeyboardMarkup();
        }
        messageService.editMessageAsync(
                new EditMessageContext(PriorityJob.Priority.HIGH)
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(reminderMessageBuilder.getReminderMessage(reminder))
                        .replyKeyboard(newKeyboard)
        );
    }

    public void sendReminderRead(long chatId, int messageId, InlineKeyboardMarkup inlineKeyboardMarkup, Reminder reminder) {
        if (reminder.isNotMySelf()) {
            messageService.editMessageAsync(
                    new EditMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(chatId)
                            .messageId(messageId)
                            .text(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getReceiverId())))
                            .replyKeyboard(new KeyboardCustomizer(inlineKeyboardMarkup).remove(CommandNames.READ_REMINDER_COMMAND_NAME).getKeyboardMarkup()));

            if (reminder.getCreatorMessageId() != null) {
                messageService.deleteMessage(reminder.getCreatorId(), reminder.getCreatorMessageId());
            }
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getCreatorId())
                            .text(reminderMessageBuilder.getReadReminderCreator(reminder))
                            .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId(), reminder.getCreator().getLocale()))
            );
        }
    }

    private void tryDeleteRemindMessage(int messageId, Reminder reminder) {
        if (reminder.hasReceiverMessage() && messageId != reminder.getReceiverMessageId()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
            reminderService.deleteReceiverMessage(reminder.getId());
        }
    }

    private void tryEditRemindMessage(int messageId, Reminder reminder, String text) {
        if (reminder.hasReceiverMessage() && messageId != reminder.getReceiverMessageId()) {
            messageService.editMessage(
                    new EditMessageContext(PriorityJob.Priority.MEDIUM)
                            .chatId(reminder.getReceiverId())
                            .messageId(reminder.getReceiverMessageId())
                            .text(text)
                            .replyKeyboard(inlineKeyboardService.getReceiverReminderKeyboard(reminder))
            );
        }
    }

    private void sendEditMessageToReceiver(Reminder reminder, String text) {
        if (reminder.hasReceiverMessage()) {
            messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
        }
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(reminder.getReceiverId())
                        .text(text)
                        .replyKeyboard(inlineKeyboardService.getOpenDetailsKeyboard(reminder.getId(), reminder.getReceiver().getLocale())),
                message -> reminderService.setReceiverMessage(reminder.getId(), message.getMessageId()));
    }
}
