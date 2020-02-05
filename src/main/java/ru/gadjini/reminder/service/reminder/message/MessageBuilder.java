package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class MessageBuilder {

    private LocalisationService localisationService;

    private TimeBuilder timeBuilder;

    @Autowired
    public MessageBuilder(LocalisationService localisationService, TimeBuilder timeBuilder) {
        this.localisationService = localisationService;
        this.timeBuilder = timeBuilder;
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, DateTime from, DateTime to) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, DateTime from, List<RepeatTime> to) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }


    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, List<RepeatTime> from, DateTime to) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }

    public String getReminderEditedReceiver(TgUser creator) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_EDITED_RECEIVER, new Object[]{UserUtils.userLink(creator)});
    }

    public String getReminderEdited(String text) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_EDITED, new Object[] {text});
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, List<RepeatTime> from, List<RepeatTime> to) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }

    public String getReminderCreator(TgUser creator) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(creator)});
    }

    public String getReminderReceiver(TgUser receiver) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(receiver)});
    }

    public String getReminderCompleted(String reminderText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_COMPLETED, new Object[]{reminderText});
    }

    public String getReminderSkipped(String reminderText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_SKIPPED, new Object[]{reminderText});
    }

    public String getReminderReturned(String reminderText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_RETURNED, new Object[]{reminderText});
    }

    public String getReminderStopped(String reminderText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_STOPPED, new Object[]{reminderText});
    }

    public String getNextReminderNotificationAt(ZonedDateTime remindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_NEXT_REMINDER_NOTIFICATION_AT, new Object[]{timeBuilder.time(remindAt)});
    }

    public String getNote(String note) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{note});
    }

    public String getNewReminder(String reminderText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_NEW_REMINDER, new Object[]{reminderText});
    }

    public String getReminderPostponed(String text, DateTime remindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED, new Object[]{text, timeBuilder.time(remindAt)});
    }

    public String getReminderNoteEditedReceiver(TgUser creator, String text, String note) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                note
        });
    }

    public String getReminderTextEdited(String oldText, String newText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED, new Object[]{oldText, newText});
    }

    public String getReminderNoteEdited(String newNote) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED, new Object[]{newNote});
    }

    public String getReminderTimeEdited(DateTime oldRemindAt, DateTime newRemindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderTimeEdited(DateTime oldRemindAt, List<RepeatTime> newRemindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderTimeEdited(List<RepeatTime> oldRemindAt, DateTime newRemindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderTimeEdited(List<RepeatTime> oldRemindAt, List<RepeatTime> newRemindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderNoteDeletedReceiver(TgUser creator, String text) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text
        });
    }

    public String getReminderNoteDeleted() {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED);
    }

    public String getReminderTextEditedReceiver(String oldText, String newText, TgUser creator) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                oldText,
                newText
        });
    }

    public String getCustomRemindCreated(ZonedDateTime remindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                timeBuilder.time(remindAt)
        });
    }

    public String getCustomRemindCreated(List<RepeatTime> repeatTimes) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                timeBuilder.time(repeatTimes)
        });
    }

    public String getNextRemindAt(DateTime remindAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_NEXT_REMINDER_NOTIFICATION_AT, new Object[]{timeBuilder.time(remindAt)});
    }

    public String getCompletedAt(ZonedDateTime completedAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_COMPLETED_AT, new Object[]{timeBuilder.time(completedAt)});
    }

    public String getReminderNotification(String reminderText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMIND, new Object[]{reminderText});
    }

    public String getItsTimeReminderNotification(String reminderText) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMIND_ITS_TIME, new Object[]{reminderText});
    }

    public String getReminderCanceled(String text) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_CANCELED, new Object[]{text});
    }

    public String getReminderDeleted(String text) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_DELETED, new Object[]{text});
    }

    public String getCurrentSeries(int currentSeries) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_CURRENT_SERIES, new Object[]{currentSeries});
    }

    public String getMaxSeries(int maxSeries) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_MAX_SERIES, new Object[]{maxSeries});
    }

    public String getTotalSeries(int totalSeries) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_TOTAL_SERIES, new Object[] {totalSeries});
    }

    public String getReminderDeactivated(String text) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_DEACTIVATED_RECEIVER, new Object[]{text});
    }

    public String getReminderActivated(String text) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_ACTIVATED_RECEIVER, new Object[]{text});
    }

    public String getReminderRead() {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_READ_REMINDER_STATUS, new Object[] {localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_READ)});
    }

    public String getReminderUnread() {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_READ_REMINDER_STATUS, new Object[] {localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_UNREAD)});
    }

    public String getReadReminderCreator(TgUser receiver, String text) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_READ_REMINDER_CREATOR, new Object[] {UserUtils.userLink(receiver), text});
    }

    public String getReminderCreatedAt(ZonedDateTime createdAt) {
        return localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_AT, new Object[] {timeBuilder.fixedDay(createdAt)});
    }
}
