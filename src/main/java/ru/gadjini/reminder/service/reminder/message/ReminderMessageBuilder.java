package ru.gadjini.reminder.service.reminder.message;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.time.DateTime;

import java.util.List;

@Service
public class ReminderMessageBuilder {

    private MessageBuilder messageBuilder;

    private TimeBuilder timeBuilder;

    @Autowired
    public ReminderMessageBuilder(MessageBuilder messageBuilder, TimeBuilder timeBuilder) {
        this.messageBuilder = messageBuilder;
        this.timeBuilder = timeBuilder;
    }

    public String getReminderMessage(Reminder reminder) {
        return getReminderMessage(reminder, reminder.getCreatorId());
    }

    public String getReminderMessage(Reminder reminder, int messageReceiverId) {
        return getReminderMessage(reminder, messageReceiverId, null);
    }

    public String getReminderMessage(Reminder reminder, int messageReceiverId, DateTime nextRemindAt) {
        StringBuilder result = new StringBuilder();
        String text = reminder.getText();
        String note = reminder.getNote();

        result.append(text).append(" ");

        if (reminder.isRepeatable()) {
            result
                    .append(timeBuilder.time(reminder.getRepeatRemindAt())).append("\n")
                    .append(messageBuilder.getNextRemindAt(nextRemindAt == null ? reminder.getRemindAtInReceiverZone() : nextRemindAt));
        } else {
            result.append(timeBuilder.time(reminder.getRemindAtInReceiverZone()));
        }
        if (reminder.getCreatorId() != reminder.getReceiverId()) {
            if (messageReceiverId == reminder.getCreatorId()) {
                result
                        .append("\n")
                        .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));
            } else if (messageReceiverId == reminder.getReceiverId()) {
                result
                        .append("\n")
                        .append(messageBuilder.getReminderCreator(reminder.getCreator()));
            }
        }

        if (StringUtils.isNotBlank(note)) {
            result.append("\n").append(messageBuilder.getNote(reminder.getNote()));
        }

        return result.toString();
    }

    public String getMySelfRepeatReminderCompleted(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n").append(messageBuilder.getNextRemindAt(reminder.getRemindAt()));

        return message.toString();
    }

    public String getRepeatReminderCompletedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getRepeatReminderCompletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCompleted(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getMySelfRepeatReminderSkipped(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderSkipped(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt()));

        return message.toString();
    }

    public String getReminderDeletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderDeleted(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderDeletedForCreator(Reminder reminder) {
        return messageBuilder.getReminderDeleted(reminder.getText());
    }

    public String getRepeatReminderStoppedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderStopped(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getCreator()));

        return message.toString();
    }

    public String getRepeatReminderStoppedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderStopped(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getMySelfRepeatReminderStopped(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderStopped(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt()));

        return message.toString();
    }

    public String getRepeatReminderSkippedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderSkipped(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getRepeatReminderSkippedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderSkipped(reminder.getText())).append("\n")
                .append(messageBuilder.getNextRemindAt(reminder.getRemindAt())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getCreator()));

        return message.toString();
    }

    public String getNewReminder(Reminder reminder, int messageReceiverId) {
        return messageBuilder.getNewReminder(getReminderMessage(reminder, messageReceiverId));
    }

    public String getRemindersList(int requesterId, List<Reminder> reminders) {
        StringBuilder text = new StringBuilder();

        int i = 1;
        for (Reminder reminder : reminders) {
            String number = i++ + ") ";
            text.append(number).append(reminder.getText()).append("(").append(timeBuilder.time(reminder)).append(")\n");

            if (reminder.getRepeatRemindAt() != null) {
                text
                        .append(" ".repeat(number.length() + 2))
                        .append(messageBuilder.getNextRemindAt(reminder.getRemindAtInReceiverZone())).append("\n");
            }

            if (reminder.getReceiverId() != reminder.getCreatorId()) {
                if (requesterId == reminder.getReceiverId()) {
                    text
                            .append(" ".repeat(number.length() + 2))
                            .append(messageBuilder.getReminderCreator(reminder.getCreator()));
                } else {
                    text
                            .append(" ".repeat(number.length() + 2))
                            .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));
                }
                text.append("\n");
            }
        }

        return text.toString();
    }

    public String getReminderTimeChanged(String text, TgUser creator, DateTime newRemindAt, DateTime oldRemindAt) {
        return messageBuilder.getReminderTimeEditedReceiver(creator, text, oldRemindAt, newRemindAt);
    }

    public String getReminderNoteChangedForReceiver(String text, String note, TgUser creator, DateTime remindAt) {
        return messageBuilder.getReminderNoteEditedReceiver(creator, text, remindAt, note);
    }

    public String getReminderNoteDeletedReceiver(String text, TgUser creator, DateTime remindAt) {
        return messageBuilder.getReminderNoteDeletedReceiver(creator, text, remindAt);
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

    public String getReminderPostponedForReceiver(String text, DateTime remindAt) {
        return messageBuilder.getReminderPostponed(text, remindAt);
    }

    public String getReminderNoteEditedReceiver(TgUser creator, String text, DateTime remindAt, String note) {
        return messageBuilder.getReminderNoteEditedReceiver(creator, text, remindAt, note);
    }

    public String getCustomRemindText(CustomRemindResult customRemindResult) {
        if (customRemindResult.isStandard()) {
            return messageBuilder.getCustomRemindCreated(customRemindResult.getZonedDateTime());
        } else {
            return messageBuilder.getCustomRemindCreated(customRemindResult.getRepeatTime());
        }
    }

    public String getReminderNoteDeletedReceiver(TgUser creator, String text, DateTime remindAt) {
        return messageBuilder.getReminderNoteDeletedReceiver(creator, text, remindAt);
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
}