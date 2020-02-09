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
import java.util.Locale;

@Service
public class MessageBuilder {

    private LocalisationService localisationService;

    private TimeBuilder timeBuilder;

    @Autowired
    public MessageBuilder(LocalisationService localisationService, TimeBuilder timeBuilder) {
        this.localisationService = localisationService;
        this.timeBuilder = timeBuilder;
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, DateTime from, DateTime to, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from, locale),
                timeBuilder.time(to, locale)
        }, locale);
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, DateTime from, List<RepeatTime> to, Locale receiverLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from, receiverLocale),
                timeBuilder.time(to, receiverLocale)
        }, receiverLocale);
    }


    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, List<RepeatTime> from, DateTime to, Locale receiverLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from, receiverLocale),
                timeBuilder.time(to, receiverLocale)
        }, receiverLocale);
    }

    public String getReminderEditedReceiver(TgUser creator, Locale receiverLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDITED_RECEIVER, new Object[]{UserUtils.userLink(creator)}, receiverLocale);
    }

    public String getReminderEdited(String text, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_EDITED, new Object[] {text}, locale);
    }

    public String getReminderTimeEditedReceiver(TgUser creator, String reminderText, List<RepeatTime> from, List<RepeatTime> to, Locale receiverLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                reminderText,
                timeBuilder.time(from, receiverLocale),
                timeBuilder.time(to, receiverLocale)
        }, receiverLocale);
    }

    public String getReminderCreator(TgUser creator) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR, new Object[]{UserUtils.userLink(creator)}, creator.getLocale());
    }

    public String getReminderReceiver(TgUser receiver) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER, new Object[]{UserUtils.userLink(receiver)}, receiver.getLocale());
    }

    public String getReminderCompleted(String reminderText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_COMPLETED, new Object[]{reminderText}, locale);
    }

    public String getReminderSkipped(String reminderText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_SKIPPED, new Object[]{reminderText}, locale);
    }

    public String getReminderReturned(String reminderText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RETURNED, new Object[]{reminderText}, locale);
    }

    public String getReminderStopped(String reminderText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_STOPPED, new Object[]{reminderText}, locale);
    }

    public String getNextReminderNotificationAt(ZonedDateTime remindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_NEXT_REMINDER_NOTIFICATION_AT, new Object[]{timeBuilder.time(remindAt, locale)}, locale);
    }

    public String getNote(String note, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{note}, locale);
    }

    public String getNewReminder(String reminderText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_NEW_REMINDER, new Object[]{reminderText}, locale);
    }

    public String getReminderPostponed(String text, DateTime remindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED, new Object[]{text, timeBuilder.time(remindAt, locale)}, locale);
    }

    public String getReminderNoteEditedReceiver(TgUser creator, String text, String note, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                note
        }, locale);
    }

    public String getReminderTextEdited(String oldText, String newText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED, new Object[]{oldText, newText}, locale);
    }

    public String getReminderNoteEdited(String newNote, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED, new Object[]{newNote}, locale);
    }

    public String getReminderTimeEdited(DateTime oldRemindAt, DateTime newRemindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt, locale), timeBuilder.time(newRemindAt, locale)}, locale);
    }

    public String getReminderTimeEdited(DateTime oldRemindAt, List<RepeatTime> newRemindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt, locale), timeBuilder.time(newRemindAt, locale)}, locale);
    }

    public String getReminderTimeEdited(List<RepeatTime> oldRemindAt, DateTime newRemindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt, locale), timeBuilder.time(newRemindAt, locale)}, locale);
    }

    public String getReminderTimeEdited(List<RepeatTime> oldRemindAt, List<RepeatTime> newRemindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED, new Object[]{timeBuilder.time(oldRemindAt, locale), timeBuilder.time(newRemindAt, locale)}, locale);
    }

    public String getReminderNoteDeletedReceiver(TgUser creator, String text, Locale receiverLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text
        }, receiverLocale);
    }

    public String getReminderNoteDeleted(Locale receiverLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED, receiverLocale);
    }

    public String getReminderTextEditedReceiver(String oldText, String newText, TgUser creator, Locale receiverLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                oldText,
                newText
        }, receiverLocale);
    }

    public String getCustomRemindCreated(ZonedDateTime remindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                timeBuilder.time(remindAt, locale)
        }, locale);
    }

    public String getCustomRemindCreated(List<RepeatTime> repeatTimes, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                timeBuilder.time(repeatTimes, locale)
        }, locale);
    }

    public String getNextRemindAt(DateTime remindAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_NEXT_REMINDER_NOTIFICATION_AT, new Object[]{timeBuilder.time(remindAt, locale)}, locale);
    }

    public String getCompletedAt(ZonedDateTime completedAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_COMPLETED_AT, new Object[]{timeBuilder.time(completedAt, locale)}, locale);
    }

    public String getReminderNotification(String reminderText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMIND, new Object[]{reminderText}, locale);
    }

    public String getItsTimeReminderNotification(String reminderText, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_ITS_TIME, new Object[]{reminderText}, locale);
    }

    public String getReminderCanceled(String text, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CANCELED, new Object[]{text}, locale);
    }

    public String getReminderDeleted(String text, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DELETED, new Object[]{text}, locale);
    }

    public String getCurrentSeries(int currentSeries, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CURRENT_SERIES, new Object[]{currentSeries}, locale);
    }

    public String getMaxSeries(int maxSeries, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_MAX_SERIES, new Object[]{maxSeries}, locale);
    }

    public String getTotalSeries(int totalSeries, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_TOTAL_SERIES, new Object[] {totalSeries}, locale);
    }

    public String getReminderDeactivated(String text, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DEACTIVATED_RECEIVER, new Object[]{text}, locale);
    }

    public String getReminderActivated(String text, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_ACTIVATED_RECEIVER, new Object[]{text}, locale);
    }

    public String getReminderRead(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_READ_REMINDER_STATUS, new Object[] {localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_READ, locale)}, locale);
    }

    public String getReminderUnread(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_READ_REMINDER_STATUS, new Object[] {localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_UNREAD, locale)}, locale);
    }

    public String getReadReminderCreator(TgUser receiver, String text, Locale creatorLocale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_READ_REMINDER_CREATOR, new Object[] {UserUtils.userLink(receiver), text}, creatorLocale);
    }

    public String getReminderCreatedAt(ZonedDateTime createdAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_AT, new Object[] {timeBuilder.fixedDay(createdAt, locale)}, locale);
    }

    public String getReason(String reason, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REASON, new Object[] {reason}, locale);
    }
}
