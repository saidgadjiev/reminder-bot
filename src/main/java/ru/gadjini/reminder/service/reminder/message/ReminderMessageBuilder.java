package ru.gadjini.reminder.service.reminder.message;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.time.ReminderTimeBuilder;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class ReminderMessageBuilder {

    private MessageBuilder messageBuilder;

    private TimeBuilder timeBuilder;

    private ReminderTimeBuilder reminderTimeBuilder;

    private LocalisationService localisationService;

    private TimeCreator timeCreator;

    @Autowired
    public ReminderMessageBuilder(MessageBuilder messageBuilder, TimeBuilder timeBuilder, ReminderTimeBuilder reminderTimeBuilder, LocalisationService localisationService, TimeCreator timeCreator) {
        this.messageBuilder = messageBuilder;
        this.timeBuilder = timeBuilder;
        this.reminderTimeBuilder = reminderTimeBuilder;
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    public String getReminderMessage(Reminder reminder) {
        return getReminderMessage(reminder, new Config().receiverId(reminder.getCreatorId()));
    }

    public String getMySelfReminderEdited(Reminder reminder) {
        return messageBuilder.getReminderEdited(reminder.getText());
    }

    public String getReminderEditedCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderEdited(reminder.getText()))
                .append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getReminderMessage(Reminder reminder, Config config) {
        StringBuilder result = new StringBuilder();
        String text = reminder.getText();
        String note = reminder.getNote();

        if (reminder.isSuppressNotifications() && config.receiverId == reminder.getReceiverId()) {
            result.append(localisationService.getMessage(MessagesProperties.SUPPRESS_NOTIFICATIONS_EMOJI)).append(" ");
        }
        result.append(text).append(" ");

        if (reminder.isInactive()) {
            result.append("(<b>").append(timeBuilder.deactivated()).append("</b>)");
        } else {
            if (reminder.isRepeatable()) {
                result
                        .append(timeBuilder.time(reminder.getRepeatRemindAtsInReceiverZone(timeCreator))).append("\n")
                        .append(messageBuilder.getNextRemindAt(config.nextRemindAt == null ? reminder.getRemindAtInReceiverZone() : config.nextRemindAt.withZoneSameInstant(reminder.getReceiverZoneId())));
            } else {
                result.append(timeBuilder.time(reminder.getRemindAtInReceiverZone()));
            }
        }

        if (!config.remindNotification) {
            result.append("\n")
                    .append(messageBuilder.getReminderCreatedAt(reminder.getCreatedAtInReceiverZone()));
        }

        if (reminder.isNotMySelf()) {
            result.append("\n")
                    .append(reminder.isRead() ? messageBuilder.getReminderRead() : messageBuilder.getReminderUnread());

            if (config.receiverId == reminder.getCreatorId()) {
                result
                        .append("\n")
                        .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));
            } else if (config.receiverId == reminder.getReceiverId()) {
                result
                        .append("\n")
                        .append(messageBuilder.getReminderCreator(reminder.getCreator()));
            }
        }

        if (reminder.isRepeatable() && reminder.isCountSeries()) {
            result
                    .append("\n\n")
                    .append(messageBuilder.getCurrentSeries(reminder.getCurrentSeries())).append("\n")
                    .append(messageBuilder.getMaxSeries(reminder.getMaxSeries())).append("\n")
                    .append(messageBuilder.getTotalSeries(reminder.getTotalSeries()));
        }

        if (StringUtils.isNotBlank(note)) {
            result.append("\n").append(messageBuilder.getNote(reminder.getNote()));
        }

        return result.toString();
    }

    public String getReminderCompletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderCompletedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getMySelfReminderCompleted(Reminder reminder) {
        return messageBuilder.getReminderCompleted(reminder.getText());
    }

    public String getMySelfRepeatReminderCompleted(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);

        return message.toString();
    }

    public String getRepeatReminderCompletedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver())).append("\n");

        return message.toString();
    }

    public String getRepeatReminderCompletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);
        message.append("\n").append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getMySelfRepeatReminderSkipped(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderSkipped(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);

        return message.toString();
    }

    public String getReminderDeletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderDeleted(reminder.getText())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderDeletedForCreator(Reminder reminder) {
        return messageBuilder.getReminderDeleted(reminder.getText());
    }

    public String getRepeatReminderStoppedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderStopped(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getCreator()));

        return message.toString();
    }

    public String getRepeatReminderStoppedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderStopped(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getMySelfRepeatReminderStopped(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderStopped(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);

        return message.toString();
    }

    public String getRepeatReminderSkippedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderSkipped(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getRepeatReminderReturnedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderReturned(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getRepeatReminderSkippedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderSkipped(reminder.getText())).append("\n");
        appendRepeatReminderCommonValues(message, reminder);
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getCreator()));

        return message.toString();
    }

    public String getNewReminder(Reminder reminder, int messageReceiverId) {
        return messageBuilder.getNewReminder(getReminderMessage(reminder, new Config().receiverId(messageReceiverId)));
    }

    public String getCompletedRemindersList(int requesterId, List<Reminder> reminders) {
        StringBuilder text = new StringBuilder();

        int i = 1;
        for (Reminder reminder : reminders) {
            String number = i++ + ") ";
            text.append(number).append(reminder.getText()).append("(").append(reminderTimeBuilder.time(reminder)).append(")\n");

            text.append(messageBuilder.getCompletedAt(reminder.getCompletedAtInReceiverZone())).append("\n");

            if (reminder.isNotMySelf()) {
                if (requesterId == reminder.getReceiverId()) {
                    text.append(messageBuilder.getReminderCreator(reminder.getCreator()));
                } else {
                    text.append(messageBuilder.getReminderReceiver(reminder.getReceiver()));
                }
                text.append("\n");
            }
        }

        return text.toString();
    }

    public String getActiveRemindersList(int requesterId, List<Reminder> reminders) {
        StringBuilder text = new StringBuilder();

        int i = 1;
        for (Reminder reminder : reminders) {
            String number = i++ + ") ";
            text.append(number);

            if (reminder.isSuppressNotifications() && requesterId == reminder.getReceiverId()) {
                text.append(localisationService.getMessage(MessagesProperties.SUPPRESS_NOTIFICATIONS_EMOJI)).append(" ");
            }
            text.append(reminder.getText()).append("(").append(reminderTimeBuilder.time(reminder)).append(")\n");

            if (!reminder.isInactive() && reminder.isRepeatable()) {
                text.append(messageBuilder.getNextRemindAt(reminder.getRemindAtInReceiverZone())).append("\n");
            }

            if (reminder.getReceiverId() != reminder.getCreatorId()) {
                if (requesterId == reminder.getReceiverId()) {
                    text.append(messageBuilder.getReminderCreator(reminder.getCreator()));
                } else {
                    text.append(messageBuilder.getReminderReceiver(reminder.getReceiver()));
                }
                text.append("\n");
            }
        }

        return text.toString();
    }

    public String getReminderTimeChanged(Reminder oldReminder, Reminder newReminder) {
        if (newReminder.isRepeatable() && oldReminder.isRepeatable()) {
            return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator));
        }
        if (newReminder.isRepeatable() && !oldReminder.isRepeatable()) {
            return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRemindAtInReceiverZone(), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator));
        }
        if (!newReminder.isRepeatable() && oldReminder.isRepeatable()) {
            return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRemindAtInReceiverZone());
        }

        return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRemindAtInReceiverZone(), newReminder.getRemindAtInReceiverZone());
    }

    public String getReminderNoteChangedForReceiver(String text, String note, TgUser creator) {
        return messageBuilder.getReminderNoteEditedReceiver(creator, text, note);
    }

    public String getReminderPostponedForCreator(String text, TgUser receiver, DateTime remindAt, String reason) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderPostponed(text, remindAt))
                .append("\n").append(messageBuilder.getReminderReceiver(receiver));

        if (StringUtils.isNotBlank(reason)) {
            message.append("\n\n").append(reason);
        }

        return message.toString();
    }

    public String getReminderPostponedForReceiver(String text, TgUser creator, DateTime remindAt, String reason) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderPostponed(text, remindAt))
                .append("\n").append(messageBuilder.getReminderCreator(creator));

        if (StringUtils.isNotBlank(reason)) {
            message.append("\n\n").append(reason);
        }

        return message.toString();
    }


    public String getMySelfReminderPostponed(String text, DateTime remindAt) {
        return messageBuilder.getReminderPostponed(text, remindAt);
    }

    public String getReminderNoteChangedReceiver(TgUser creator, String text, String note) {
        return messageBuilder.getReminderNoteEditedReceiver(creator, text, note);
    }

    public String getCustomRemindText(CustomRemindResult customRemindResult) {
        ZoneId receiverZoneId = customRemindResult.getReceiverZoneId();

        if (customRemindResult.isStandard()) {
            return messageBuilder.getCustomRemindCreated(customRemindResult.getZonedDateTime().withZoneSameInstant(receiverZoneId));
        } else {
            return messageBuilder.getCustomRemindCreated(timeCreator.withZone(customRemindResult.getRepeatTimes(), receiverZoneId));
        }
    }

    public String getReminderNoteDeletedReceiver(TgUser creator, String text) {
        return messageBuilder.getReminderNoteDeletedReceiver(creator, text);
    }

    public String getReminderTextChanged(String oldText, String newText, TgUser creator) {
        return messageBuilder.getReminderTextEditedReceiver(oldText, newText, creator);
    }

    public String getMySelfReminderCanceled(Reminder reminder) {
        return messageBuilder.getReminderCanceled(reminder.getText());
    }

    public String getReminderCanceledForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCanceled(reminder.getText())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderCanceledForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCanceled(reminder.getText())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getReminderDeactivatedReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderDeactivated(reminder.getText())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderActivatedReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderActivated(reminder.getText())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReadReminderCreator(Reminder reminder) {
        return messageBuilder.getReadReminderCreator(reminder.getReceiver(), reminder.getText());
    }

    public String getFullyUpdateMessageForReceiver(Reminder oldReminder, Reminder newReminder) {
        Map<Field<?>, Object> diff = oldReminder.getDiff(newReminder);

        if (diff.size() == 1) {
            if (diff.containsKey(ReminderTable.TABLE.TEXT)) {
                return getReminderTextChanged(oldReminder.getText(), newReminder.getText(), oldReminder.getCreator());
            }
            if (diff.containsKey(ReminderTable.TABLE.NOTE)) {
                if (diff.get(ReminderTable.TABLE.NOTE) == null) {
                    return getReminderNoteDeletedReceiver(oldReminder.getCreator(), oldReminder.getText());
                } else {
                    return getReminderNoteChangedReceiver(oldReminder.getCreator(), oldReminder.getText(), newReminder.getNote());
                }
            }
            if (diff.containsKey(ReminderTable.TABLE.REMIND_AT)) {
                return getReminderTimeChanged(oldReminder, newReminder);
            }
            if (diff.containsKey(ReminderTable.TABLE.REPEAT_REMIND_AT)) {
                return getReminderTimeChanged(oldReminder, newReminder);
            }
        } else {
            StringBuilder message = new StringBuilder();
            message.append(messageBuilder.getReminderEditedReceiver(oldReminder.getCreator())).append("\n\n");
            if (diff.containsKey(ReminderTable.TABLE.TEXT)) {
                message.append(messageBuilder.getReminderTextEdited(oldReminder.getText(), newReminder.getText())).append("\n\n");
            }
            if (diff.containsKey(ReminderTable.TABLE.NOTE)) {
                if (diff.get(ReminderTable.TABLE.NOTE) == null) {
                    message.append(messageBuilder.getReminderNoteDeleted()).append("\n\n");
                } else {
                    message.append(messageBuilder.getReminderNoteEdited(newReminder.getNote())).append("\n\n");
                }
            }
            if (diff.containsKey(ReminderTable.TABLE.REMIND_AT)) {
                message.append(getReminderTimeEdited(oldReminder, newReminder));
            } else if (diff.containsKey(ReminderTable.TABLE.REPEAT_REMIND_AT)) {
                message.append(getReminderTimeEdited(oldReminder, newReminder));
            }

            return message.toString();
        }

        throw new IllegalArgumentException("Reminder not changed");
    }

    private String getReminderTimeEdited(Reminder oldReminder, Reminder newReminder) {
        if (newReminder.isRepeatable() && oldReminder.isRepeatable()) {
            return messageBuilder.getReminderTimeEdited(oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator));
        }
        if (newReminder.isRepeatable() && !oldReminder.isRepeatable()) {
            return messageBuilder.getReminderTimeEdited(oldReminder.getRemindAtInReceiverZone(), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator));
        }
        if (!newReminder.isRepeatable() && oldReminder.isRepeatable()) {
            return messageBuilder.getReminderTimeEdited(oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRemindAtInReceiverZone());
        }

        return messageBuilder.getReminderTimeEdited(oldReminder.getRemindAtInReceiverZone(), newReminder.getRemindAtInReceiverZone());
    }

    private void appendRepeatReminderCommonValues(StringBuilder message, Reminder reminder) {
        message.append(messageBuilder.getNextRemindAt(reminder.getRemindAtInReceiverZone()));

        if (reminder.isCountSeries()) {
            message
                    .append("\n")
                    .append(messageBuilder.getCurrentSeries(reminder.getCurrentSeries())).append("\n")
                    .append(messageBuilder.getMaxSeries(reminder.getMaxSeries())).append("\n")
                    .append(messageBuilder.getTotalSeries(reminder.getTotalSeries()));
        }
    }

    public static class Config {

        private boolean remindNotification;

        private int receiverId;

        private DateTime nextRemindAt;

        public boolean remindNotification() {
            return this.remindNotification;
        }

        public Config remindNotification(final boolean remindNotification) {
            this.remindNotification = remindNotification;
            return this;
        }

        public int receiverId() {
            return receiverId;
        }

        public Config receiverId(final int receiverId) {
            this.receiverId = receiverId;

            return this;
        }

        public DateTime nextRemindAt() {
            return this.nextRemindAt;
        }

        public Config nextRemindAt(final DateTime nextRemindAt) {
            this.nextRemindAt = nextRemindAt;
            return this;
        }
    }
}