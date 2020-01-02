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
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, DateTime from, RepeatTime to) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }


    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, RepeatTime from, DateTime to) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }

    public String getReminderEditedReceiver(TgUser creator) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDITED_RECEIVER, new Object[]{UserUtils.userLink(creator)});
    }

    public String getReminderEdited() {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDITED);
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, RepeatTime from, RepeatTime to) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from),
                timeBuilder.time(to)
        });
    }

    public String getReminderCreator(TgUser creator) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(creator)});
    }

    public String getReminderReceiver(TgUser receiver) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(receiver)});
    }

    public String getReminderCompleted(String reminderText) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_COMPLETED, new Object[]{reminderText});
    }

    public String getReminderSkipped(String reminderText) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_SKIPPED, new Object[]{reminderText});
    }

    public String getReminderStopped(String reminderText) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_STOPPED, new Object[]{reminderText});
    }

    public String getNextReminderNotificationAt(ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_NEXT_REMINDER_NOTIFICATION_AT, new Object[]{timeBuilder.time(remindAt)});
    }

    public String getNote(String note) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{note});
    }

    public String getNewReminder(String reminderText) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_NEW_REMINDER, new Object[]{reminderText});
    }

    public String getReminderPostponed(String text, DateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED, new Object[]{text, timeBuilder.time(remindAt)});
    }

    public String getReminderNoteEditedReceiver(TgUser creator, String text, String note) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                note
        });
    }

    public String getReminderTextEdited(String oldText, String newText) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED, new Object[]{oldText, newText});
    }

    public String getReminderNoteEdited(String newNote) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED, new Object[]{newNote});
    }

    public String getReminderTimeEdited(DateTime oldRemindAt, DateTime newRemindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderTimeEdited(DateTime oldRemindAt, RepeatTime newRemindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderTimeEdited(RepeatTime oldRemindAt, DateTime newRemindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderTimeEdited(RepeatTime oldRemindAt, RepeatTime newRemindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt), timeBuilder.time(newRemindAt)});
    }

    public String getReminderNoteDeletedReceiver(TgUser creator, String text) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text
        });
    }

    public String getReminderNoteDeleted() {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED);
    }

    public String getReminderTextEditedReceiver(String oldText, String newText, TgUser creator) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                oldText,
                newText
        });
    }

    public String getCustomRemindCreated(ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                timeBuilder.time(remindAt)
        });
    }

    public String getCustomRemindCreated(RepeatTime repeatTime) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                timeBuilder.time(repeatTime)
        });
    }

    public String getNextRemindAt(DateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_NEXT_REMINDER_NOTIFICATION_AT, new Object[]{timeBuilder.time(remindAt)});
    }

    public String getCompletedAt(ZonedDateTime completedAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_AT, new Object[]{timeBuilder.time(completedAt)});
    }

    public String getReminderNotification(String reminderText) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMIND, new Object[]{reminderText});
    }

    public String getItsTimeReminderNotification(String reminderText) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_ITS_TIME, new Object[]{reminderText});
    }

    public String getReminderCanceled(String text) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANCELED, new Object[]{text});
    }

    public String getReminderDeleted(String text) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DELETED, new Object[]{text});
    }
}
