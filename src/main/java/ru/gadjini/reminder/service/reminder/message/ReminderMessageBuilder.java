package ru.gadjini.reminder.service.reminder.message;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.model.CustomRemindResult;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.time.ReminderTimeBuilder;
import ru.gadjini.reminder.service.reminder.time.Time2TextService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.ActionResult;
import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.ReminderActionResult;

@Service
public class ReminderMessageBuilder {

    private ReminderLocalisationService messageBuilder;

    private Time2TextService timeBuilder;

    private ReminderTimeBuilder reminderTimeBuilder;

    private DateTimeService timeCreator;

    private LocalisationService localisationService;

    @Autowired
    public ReminderMessageBuilder(ReminderLocalisationService messageBuilder, Time2TextService timeBuilder,
                                  ReminderTimeBuilder reminderTimeBuilder, DateTimeService timeCreator,
                                  LocalisationService localisationService) {
        this.messageBuilder = messageBuilder;
        this.timeBuilder = timeBuilder;
        this.reminderTimeBuilder = reminderTimeBuilder;
        this.timeCreator = timeCreator;
        this.localisationService = localisationService;
    }

    public String getReminderMessage(Reminder reminder) {
        return getReminderMessage(reminder, new ReminderMessageConfig().receiverId(reminder.getCreatorId()));
    }

    public String getMySelfReminderEdited(Reminder reminder) {
        return messageBuilder.getReminderEdited(reminder.getText(), reminder.getReceiver().getLocale());
    }

    public String getReminderEditedCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderEdited(reminder.getText(), reminder.getCreator().getLocale()))
                .append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getReminderMessage(Reminder reminder, ReminderMessageConfig config) {
        StringBuilder result = new StringBuilder();
        String text = reminder.getText();
        String note = reminder.getNote();

        Locale locale = config.receiverId == reminder.getCreatorId() ? reminder.getCreator().getLocale() : reminder.getReceiver().getLocale();
        List<String> icons = getReminderIcons(config.receiverId, reminder, locale);
        for (String icon : icons) {
            result.append(icon);
        }
        if (icons.size() > 0) {
            result.append(" ");
        }
        result.append(text).append(" ");

        if (reminder.isInactive()) {
            result.append("(<b>").append(timeBuilder.deactivated(locale)).append("</b>)");
        } else {
            if (reminder.isRepeatableWithTime() || reminder.isRepeatableWithoutTime()) {
                result
                        .append(timeBuilder.time(reminder.getRepeatRemindAtsInReceiverZone(timeCreator), locale));

                if (reminder.isRepeatableWithTime()) {
                    result.append("\n").append(messageBuilder.getNextRemindAt(config.nextRemindAt == null ? reminder.getRemindAtInReceiverZone() : config.nextRemindAt.withZoneSameInstant(reminder.getReceiverZoneId()), locale));
                }
                RepeatTime repeatTime = reminder.getRepeatRemindAt();
                if (repeatTime.hasSeriesToComplete()) {
                    result.append("\n").append(messageBuilder.getSeriesToComplete(reminder.getCurrSeriesToComplete(), repeatTime.getSeriesToComplete(), locale));
                }
            } else {
                result.append(timeBuilder.time(reminder.getRemindAtInReceiverZone(), locale));
            }
        }

        if (!config.remindNotification) {
            result.append("\n")
                    .append(messageBuilder.getReminderCreatedAt(reminder.getCreatedAtInReceiverZone(), locale));
        }

        if (reminder.isNotMySelf()) {
            result.append("\n")
                    .append(reminder.isRead() ? messageBuilder.getReminderRead(locale) : messageBuilder.getReminderUnread(locale));

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
        if (reminder.getChallengeId() != null && !reminder.isCountSeries() || reminder.isRepeatableWithoutTime()) {
            result
                    .append("\n\n")
                    .append(messageBuilder.getTotalSeries(reminder.getTotalSeries(), locale));
        } else if (reminder.isRepeatableWithTime() && reminder.isCountSeries()) {
            result
                    .append("\n\n")
                    .append(messageBuilder.getCurrentSeries(reminder.getCurrentSeries(), locale)).append("\n")
                    .append(messageBuilder.getMaxSeries(reminder.getMaxSeries(), locale)).append("\n")
                    .append(messageBuilder.getTotalSeries(reminder.getTotalSeries(), locale));
        }
        if (reminder.isTimeTracker()) {
            result
                    .append("\n\n")
                    .append(messageBuilder.getEstimate(reminder.getEstimate(), locale));
            if (reminder.getElapsedTime() != null) {
                result.append("\n").append(messageBuilder.getElapsedTime(reminder.getElapsedTime(), locale));
            }
            if (reminder.getStatus() == Reminder.Status.IN_PROGRESS) {
                result.append("\n").append(messageBuilder.getWorkInProgress(reminder.getLastWorkInProgressAtInReceiverZone(), locale));
            }
        }

        if (StringUtils.isNotBlank(note)) {
            result.append("\n\n").append(messageBuilder.getNote(reminder.getNote(), locale));
        }

        return result.toString();
    }

    public String getReminderCompletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCompleted(reminder.getText(), reminder.getReceiver().getLocale())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderCompletedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCompleted(reminder.getText(), reminder.getCreator().getLocale())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getMySelfReminderCompleted(Reminder reminder) {
        return messageBuilder.getReminderCompleted(reminder.getText(), reminder.getReceiver().getLocale());
    }

    public String getMySelfRepeatReminderCompleted(ReminderActionResult reminderActionResult) {
        Reminder reminder = reminderActionResult.getReminder();
        if (reminderActionResult.getActionResult() == ActionResult.CURR_SERIES_TO_COMPLETE_CHANGED) {
            return getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getReceiverId()));
        }
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderCompleted(reminder.getText(), reminder.getReceiver().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, true, reminder, reminder.getReceiver().getLocale());

        return message.toString();
    }

    public String getRepeatReminderCompletedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderCompleted(reminder.getText(), reminder.getCreator().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, true, reminder, reminder.getCreator().getLocale());
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver())).append("\n");

        return message.toString();
    }

    public String getRepeatReminderCompletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderCompleted(reminder.getText(), reminder.getReceiver().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, true, reminder, reminder.getReceiver().getLocale());
        message.append("\n").append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getMySelfRepeatReminderSkipped(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderSkipped(reminder.getText(), reminder.getReceiver().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, true, reminder, reminder.getReceiver().getLocale());

        return message.toString();
    }

    public String getReminderDeletedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderDeleted(reminder.getText(), reminder.getReceiver().getLocale())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderDeletedForCreator(Reminder reminder) {
        return messageBuilder.getReminderDeleted(reminder.getText(), reminder.getReceiver().getLocale());
    }

    public String getRepeatReminderStoppedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderStopped(reminder.getText(), reminder.getReceiver().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, false, reminder, reminder.getReceiver().getLocale());
        message.append("\n").append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getRepeatReminderStoppedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderStopped(reminder.getText(), reminder.getCreator().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, false, reminder, reminder.getCreator().getLocale());
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getMySelfRepeatReminderStopped(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderStopped(reminder.getText(), reminder.getReceiver().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, false, reminder, reminder.getReceiver().getLocale());

        return message.toString();
    }

    public String getRepeatReminderSkippedForReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderSkipped(reminder.getText(), reminder.getReceiver().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, true, reminder, reminder.getReceiver().getLocale());
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getRepeatReminderReturnedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderReturned(reminder.getText(), reminder.getCreator().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, true, reminder, reminder.getCreator().getLocale());
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        return message.toString();
    }

    public String getRepeatReminderSkippedForCreator(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderSkipped(reminder.getText(), reminder.getCreator().getLocale())).append("\n");
        appendRepeatReminderCommonValues(message, true, reminder, reminder.getCreator().getLocale());
        message.append("\n").append(messageBuilder.getReminderReceiver(reminder.getCreator()));

        return message.toString();
    }

    public String getNewReminder(Reminder reminder, int messageReceiverId) {
        return messageBuilder.getNewReminder(getReminderMessage(reminder, new ReminderMessageConfig().receiverId(messageReceiverId)), messageReceiverId == reminder.getReceiverId() ? reminder.getReceiver().getLocale() : reminder.getCreator().getLocale());
    }

    public String getCompletedRemindersList(int requesterId, List<Reminder> reminders, Locale locale) {
        StringBuilder text = new StringBuilder();

        int i = 1;
        for (Reminder reminder : reminders) {
            String number = i++ + ") ";
            text.append(number).append(reminder.getText()).append("(").append(reminderTimeBuilder.time(reminder, locale)).append(")\n");

            text.append(messageBuilder.getCompletedAt(reminder.getCompletedAtInReceiverZone(), reminder.getReceiver().getLocale())).append("\n");

            if (reminder.isTimeTracker()) {
                text.append(messageBuilder.getEstimate(reminder.getEstimate(), locale));
                if (reminder.getElapsedTime() != null) {
                    text.append("\n").append(messageBuilder.getElapsedTime(reminder.getElapsedTime(), locale));
                }
            }

            if (reminder.isNotMySelf()) {
                if (requesterId == reminder.getReceiverId()) {
                    text.append(messageBuilder.getReminderCreator(reminder.getCreator()));
                } else {
                    text.append(messageBuilder.getReminderReceiver(reminder.getReceiver()));
                }
                text.append("\n");
            }
            if (StringUtils.isNotBlank(reminder.getNote())) {
                text.append("\n").append(messageBuilder.getNote(reminder.getNote(), locale));
            }
            text.append("\n");
        }

        return text.toString();
    }

    public String getActiveRemindersList(int requesterId, List<Reminder> reminders, String header, Locale locale) {
        StringBuilder text = new StringBuilder();

        if (StringUtils.isNotBlank(header)) {
            text.append(localisationService.getMessage(header, locale)).append("\n\n");
        }
        if (reminders.isEmpty()) {
            text.append(messageBuilder.getRemindersEmpty(locale));

            return text.toString();
        }

        int i = 1;
        for (Reminder reminder : reminders) {
            String number = i++ + ") ";
            text.append(number);
            List<String> icons = getReminderIcons(requesterId, reminder, locale);
            for (String icon : icons) {
                text.append(icon);
            }
            if (icons.size() > 0) {
                text.append(" ");
            }
            text.append(reminder.getText());
            String time = reminderTimeBuilder.time(reminder, locale);
            if (StringUtils.isNotBlank(time)) {
                text.append("(").append(time).append(")");
            }
            text.append("\n");

            if (!reminder.isInactive() && reminder.isRepeatableWithTime()) {
                text.append(messageBuilder.getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getReceiver().getLocale())).append("\n");
            }

            if (!Objects.equals(reminder.getReceiverId(), reminder.getCreatorId())) {
                if (requesterId == reminder.getReceiverId()) {
                    text.append(messageBuilder.getReminderCreator(reminder.getCreator()));
                } else {
                    text.append(messageBuilder.getReminderReceiver(reminder.getReceiver()));
                }
                text.append("\n");
            }
        }
        String textStr = text.toString();

        return textStr.substring(0, textStr.length() - 1);
    }

    public String getReminderTimeChanged(Reminder oldReminder, Reminder newReminder) {
        if (newReminder.isRepeatableWithTime() && oldReminder.isRepeatableWithTime()) {
            return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator), oldReminder.getReceiver().getLocale());
        }
        if (newReminder.isRepeatableWithTime() && !oldReminder.isRepeatableWithTime()) {
            return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRemindAtInReceiverZone(), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator), oldReminder.getReceiver().getLocale());
        }
        if (!newReminder.isRepeatableWithTime() && oldReminder.isRepeatableWithTime()) {
            return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRemindAtInReceiverZone(), oldReminder.getReceiver().getLocale());
        }

        return messageBuilder.getReminderTimeEditedReceiver(oldReminder.getCreator(), oldReminder.getText(), oldReminder.getRemindAtInReceiverZone(), newReminder.getRemindAtInReceiverZone(), oldReminder.getReceiver().getLocale());
    }

    public String getReminderNoteChangedForReceiver(String text, String note, TgUser creator, Locale receiverLocale) {
        return messageBuilder.getReminderNoteEditedReceiver(creator, text, note, receiverLocale);
    }

    public String getReminderPostponedForCreator(String text, TgUser receiver, DateTime remindAt, String reason, Locale creatorLocale) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderPostponed(text, remindAt, creatorLocale))
                .append("\n").append(messageBuilder.getReminderReceiver(receiver));

        if (StringUtils.isNotBlank(reason)) {
            message.append("\n\n").append(messageBuilder.getReason(reason, creatorLocale));
        }

        return message.toString();
    }

    public String getReminderPostponedForReceiver(String text, TgUser creator, DateTime remindAt, String reason, Locale receiverLocale) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderPostponed(text, remindAt, receiverLocale))
                .append("\n").append(messageBuilder.getReminderCreator(creator));

        if (StringUtils.isNotBlank(reason)) {
            message.append("\n\n").append(messageBuilder.getReason(reason, receiverLocale));
        }

        return message.toString();
    }


    public String getMySelfReminderPostponed(String text, DateTime remindAt, Locale locale) {
        return messageBuilder.getReminderPostponed(text, remindAt, locale);
    }

    public String getReminderNoteChangedReceiver(TgUser creator, String text, String note, Locale receiverLocale) {
        return messageBuilder.getReminderNoteEditedReceiver(creator, text, note, receiverLocale);
    }

    public String getCustomRemindText(CustomRemindResult customRemindResult) {
        ZoneId receiverZoneId = customRemindResult.getReceiverZoneId();

        if (customRemindResult.isStandard()) {
            return messageBuilder.getCustomRemindCreated(customRemindResult.getZonedDateTime().withZoneSameInstant(receiverZoneId), customRemindResult.getReminder().getReceiver().getLocale());
        } else {
            return messageBuilder.getCustomRemindCreated(timeCreator.withZone(customRemindResult.getRepeatTimes(), receiverZoneId), customRemindResult.getReminder().getReceiver().getLocale());
        }
    }

    public String getReminderNoteDeletedReceiver(Reminder reminder) {
        return messageBuilder.getReminderNoteDeletedReceiver(reminder.getCreator(), reminder.getText(), reminder.getReceiver().getLocale());
    }

    public String getReminderTextChanged(String oldText, String newText, TgUser creator, Locale receiverLocale) {
        return messageBuilder.getReminderTextEditedReceiver(oldText, newText, creator, receiverLocale);
    }

    public String getMySelfReminderCanceled(Reminder reminder) {
        return messageBuilder.getReminderCanceled(reminder.getText(), reminder.getReceiver().getLocale());
    }

    public String getReminderCanceledForReceiver(Reminder reminder, String reason) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCanceled(reminder.getText(), reminder.getReceiver().getLocale())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        if (StringUtils.isNotBlank(reason)) {
            message.append("\n\n").append(messageBuilder.getReason(reason, reminder.getReceiver().getLocale()));
        }

        return message.toString();
    }

    public String getReminderCanceledForCreator(Reminder reminder, String reason) {
        StringBuilder message = new StringBuilder();

        message
                .append(messageBuilder.getReminderCanceled(reminder.getText(), reminder.getCreator().getLocale())).append("\n")
                .append(messageBuilder.getReminderReceiver(reminder.getReceiver()));

        if (StringUtils.isNotBlank(reason)) {
            message.append("\n\n").append(messageBuilder.getReason(reason, reminder.getCreator().getLocale()));
        }

        return message.toString();
    }

    public String getReminderDeactivatedReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderDeactivated(reminder.getText(), reminder.getReceiver().getLocale())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReminderActivatedReceiver(Reminder reminder) {
        StringBuilder message = new StringBuilder();

        message.append(messageBuilder.getReminderActivated(reminder.getText(), reminder.getReceiver().getLocale())).append("\n")
                .append(messageBuilder.getReminderCreator(reminder.getCreator()));

        return message.toString();
    }

    public String getReadReminderCreator(Reminder reminder) {
        return messageBuilder.getReadReminderCreator(reminder.getReceiver(), reminder.getText(), reminder.getCreator().getLocale());
    }

    public String getFullyUpdateMessageForReceiver(Reminder oldReminder, Reminder newReminder) {
        Map<Field<?>, Object> diff = oldReminder.getDiff(newReminder);

        if (diff.size() == 1) {
            if (diff.containsKey(ReminderTable.TABLE.TEXT)) {
                return getReminderTextChanged(oldReminder.getText(), newReminder.getText(), oldReminder.getCreator(), oldReminder.getReceiver().getLocale());
            }
            if (diff.containsKey(ReminderTable.TABLE.NOTE)) {
                if (diff.get(ReminderTable.TABLE.NOTE) == null) {
                    return getReminderNoteDeletedReceiver(oldReminder);
                } else {
                    return getReminderNoteChangedReceiver(oldReminder.getCreator(), oldReminder.getText(), newReminder.getNote(), oldReminder.getReceiver().getLocale());
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
            message.append(messageBuilder.getReminderEditedReceiver(oldReminder.getCreator(), oldReminder.getReceiver().getLocale())).append("\n\n");
            if (diff.containsKey(ReminderTable.TABLE.TEXT)) {
                message.append(messageBuilder.getReminderTextEdited(oldReminder.getText(), newReminder.getText(), oldReminder.getReceiver().getLocale())).append("\n\n");
            }
            if (diff.containsKey(ReminderTable.TABLE.NOTE)) {
                if (diff.get(ReminderTable.TABLE.NOTE) == null) {
                    message.append(messageBuilder.getReminderNoteDeleted(oldReminder.getReceiver().getLocale())).append("\n\n");
                } else {
                    message.append(messageBuilder.getReminderNoteEdited(newReminder.getNote(), oldReminder.getReceiver().getLocale())).append("\n\n");
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
        if (newReminder.isRepeatableWithTime() && oldReminder.isRepeatableWithTime()) {
            return messageBuilder.getReminderTimeEdited(oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator), oldReminder.getReceiver().getLocale());
        }
        if (newReminder.isRepeatableWithTime() && !oldReminder.isRepeatableWithTime()) {
            return messageBuilder.getReminderTimeEdited(oldReminder.getRemindAtInReceiverZone(), newReminder.getRepeatRemindAtsInReceiverZone(timeCreator), oldReminder.getReceiver().getLocale());
        }
        if (!newReminder.isRepeatableWithTime() && oldReminder.isRepeatableWithTime()) {
            return messageBuilder.getReminderTimeEdited(oldReminder.getRepeatRemindAtsInReceiverZone(timeCreator), newReminder.getRemindAtInReceiverZone(), oldReminder.getReceiver().getLocale());
        }

        return messageBuilder.getReminderTimeEdited(oldReminder.getRemindAtInReceiverZone(), newReminder.getRemindAtInReceiverZone(), oldReminder.getReceiver().getLocale());
    }

    private void appendRepeatReminderCommonValues(StringBuilder message, boolean appendNextRemindAt, Reminder reminder, Locale locale) {
        if (appendNextRemindAt && reminder.isRepeatableWithTime()) {
            message.append(messageBuilder.getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getReceiver().getLocale()));
        }
        if (reminder.getRepeatRemindAt().hasSeriesToComplete()) {
            message.append("\n").append(messageBuilder.getSeriesToComplete(reminder.getCurrSeriesToComplete(), reminder.getRepeatRemindAt().getSeriesToComplete(), locale));
        }

        if (reminder.isRepeatableWithoutTime()) {
            message
                    .append("\n\n")
                    .append(messageBuilder.getTotalSeries(reminder.getTotalSeries(), locale));
        } else if (reminder.isCountSeries()) {
            message
                    .append("\n\n")
                    .append(messageBuilder.getCurrentSeries(reminder.getCurrentSeries(), locale)).append("\n")
                    .append(messageBuilder.getMaxSeries(reminder.getMaxSeries(), locale)).append("\n")
                    .append(messageBuilder.getTotalSeries(reminder.getTotalSeries(), locale));
        }
    }

    private List<String> getReminderIcons(int requesterId, Reminder reminder, Locale locale) {
        List<String> icons = new ArrayList<>();

        if (!reminder.isRepeatable() && isExpired(reminder.getRemindAt())) {
            icons.add(messageBuilder.getExpiredIcon(locale));
        }
        if (reminder.getChallengeId() != null) {
            icons.add(messageBuilder.getReminderChallenge(locale));
        }
        if (reminder.isSuppressNotifications() && requesterId == reminder.getReceiverId()) {
            icons.add(messageBuilder.getSuppressNotificationsIcon(locale));
        }

        return icons;
    }

    private boolean isExpired(DateTime remindAt) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZoneId());
        if (remindAt.hasTime()) {
            return now.isAfter(remindAt.toZonedDateTime());
        }

        return now.toLocalDate().isAfter(remindAt.date());
    }

    public static class ReminderMessageConfig {

        private boolean remindNotification;

        private int receiverId;

        private DateTime nextRemindAt;

        public boolean remindNotification() {
            return this.remindNotification;
        }

        public ReminderMessageConfig remindNotification(final boolean remindNotification) {
            this.remindNotification = remindNotification;
            return this;
        }

        public int receiverId() {
            return receiverId;
        }

        public ReminderMessageConfig receiverId(final int receiverId) {
            this.receiverId = receiverId;

            return this;
        }

        public DateTime nextRemindAt() {
            return this.nextRemindAt;
        }

        public ReminderMessageConfig nextRemindAt(final DateTime nextRemindAt) {
            this.nextRemindAt = nextRemindAt;
            return this;
        }
    }
}