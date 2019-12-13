package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
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

    public String getReminderMessage(Reminder reminder) {
        StringBuilder result = new StringBuilder();
        String text = reminder.getText();
        String note = reminder.getNote();

        result.append(text).append(" ");

        if (reminder.isRepeatable()) {
            result.append(timeBuilder.time(reminder.getRepeatRemindAt()));
            result.append("\n").append(
                    localisationService.getMessage(
                            MessagesProperties.MESSAGE_NEXT_REMIND_AT,
                            new Object[]{timeBuilder.time(reminder.getRemindAt().withZoneSameInstant(reminder.getReceiverZoneId()))}
                    )
            );
        } else {
            result.append(timeBuilder.time(reminder.getRemindAt()));
        }

        if (StringUtils.isNotBlank(note)) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    note
            }));
        }

        return result.toString();
    }

    public String getReminderCreatedForReceiver(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_RECEIVER, new Object[]{
                reminder.getText() + " " + timeBuilder.time(reminder.getRemindAtInReceiverZone()),
                UserUtils.userLink(reminder.getCreator())
        }));

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    reminder.getNote()
            }));
        }

        return result.toString();
    }

    public String getReminderCreatedForCreator(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_CREATOR, new Object[]{
                reminder.getText() + " " + timeBuilder.time(reminder.getRemindAt().withZoneSameInstant(reminder.getReceiverZoneId())),
                UserUtils.userLink(reminder.getReceiver())
        }));

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    reminder.getNote()
            }));
        }

        return result.toString();
    }

    public String getMySelfReminderCreated(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_ME, new Object[]{
                reminder.getText() + " " + timeBuilder.time(reminder.getRemindAt().withZoneSameInstant(reminder.getReceiverZoneId()))
        }));

        if (reminder.getRepeatRemindAt() != null) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_NEXT_REMIND_AT, new Object[]{timeBuilder.time(reminder.getRemindAtInReceiverZone())}));
        }

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result
                    .append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                            reminder.getNote()
                    }));
        }

        return result.toString();
    }

    public String getRemindForReceiver(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (itsTime) {
            return getItsTimeRemindForReceiver(reminder, nextRemindAt);
        } else {
            return getRemindForReceiver(reminder);
        }
    }

    public String getRemindMySelf(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (itsTime) {
            return getItsTimeRemindMySelf(reminder, nextRemindAt);
        } else {
            return getRemindMySelf(reminder);
        }
    }

    public String getReminderTimeChanged(String text, TgUser creator, DateTime newRemindAt, DateTime oldRemindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                timeBuilder.time(oldRemindAt),
                timeBuilder.time(newRemindAt)
        });
    }

    public String getReminderNoteChangedForReceiver(String text, String note, TgUser creator, DateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                timeBuilder.time(remindAt),
                note
        });
    }

    public String getReminderNoteDeletedForReceiver(String text, TgUser creator, DateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                timeBuilder.time(remindAt)
        });
    }

    public String getReminderPostponedForCreator(String text, TgUser receiver, DateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED_CREATOR, new Object[]{
                UserUtils.userLink(receiver),
                text,
                timeBuilder.postponeTime(remindAt.toZonedDateTime())
        });
    }

    public String getRemindersListInfo(int requesterId, List<Reminder> reminders) {
        StringBuilder text = new StringBuilder();

        int i = 1;
        for (Reminder reminder : reminders) {
            String number = i++ + ") ";
            text.append(number).append(reminder.getText()).append("(").append(timeBuilder.time(reminder)).append(")\n");

            if (reminder.getRepeatRemindAt() != null) {
                text
                        .append(" ".repeat(number.length() + 2))
                        .append(localisationService.getMessage(MessagesProperties.MESSAGE_NEXT_REMIND_AT, new Object[]{timeBuilder.time(reminder.getRemindAtInReceiverZone())})).append("\n");
            }

            if (reminder.getReceiverId() != reminder.getCreatorId()) {
                if (requesterId == reminder.getReceiverId()) {
                    text
                            .append(" ".repeat(number.length() + 2))
                            .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATOR)).append(": ")
                            .append(UserUtils.userLink(reminder.getCreator()));
                } else {
                    text.append(" ".repeat(number.length() + 2))
                            .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_RECEIVER))
                            .append(": ").append(UserUtils.userLink(reminder.getReceiver()));
                }
                text.append("\n");
            }
        }

        return text.toString();
    }

    public String getReminderPostponedForReceiver(String text, DateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED_RECEIVER, new Object[]{
                text,
                timeBuilder.postponeTime(remindAt.toZonedDateTime())
        });
    }

    public String getReminderTextChanged(String oldText, String newText, TgUser creator) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                oldText,
                newText
        });
    }

    public String getCustomRemindText(ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                timeBuilder.time(remindAt)
        });
    }

    private String getNextRemindAt(DateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_NEXT_REMIND_AT, new Object[]{timeBuilder.time(remindAt)});
    }

    private String getRemindForReceiver(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND, new Object[]{
                reminder.getText() + " " + timeBuilder.time(reminder.getRemindAtInReceiverZone()),
                UserUtils.userLink(reminder.getCreator())
        }));

        if (reminder.isRepeatable()) {
            String nextRemindAt = getNextRemindAt(reminder.getRemindAtInReceiverZone());
            result.append("\n").append(nextRemindAt);
        }

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    reminder.getNote()
            }));
        }

        return result.toString();
    }

    private String getItsTimeRemindForReceiver(Reminder reminder, DateTime nextRemindAt) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_ITS_TIME, new Object[]{
                reminder.getText() + " " + timeBuilder.time(reminder.getRemindAtInReceiverZone()),
                UserUtils.userLink(reminder.getCreator())
        }));

        if (reminder.isRepeatable()) {
            result.append("\n").append(nextRemindAt);
        }

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    reminder.getNote()
            }));
        }

        return result.toString();
    }

    private String getRemindMySelf(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_ME, new Object[]{
                reminder.getText() + " " + timeBuilder.time(reminder.getRemindAt().withZoneSameInstant(reminder.getReceiverZoneId()))
        }));

        if (reminder.isRepeatable()) {
            String nextRemindAt = getNextRemindAt(reminder.getRemindAtInReceiverZone());
            result.append("\n").append(nextRemindAt);
        }

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result
                    .append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                            reminder.getNote()
                    }));
        }

        return result.toString();
    }

    private String getItsTimeRemindMySelf(Reminder reminder, DateTime nextRemindAt) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_ME_ITS_TIME, new Object[]{
                reminder.getText() + " " + timeBuilder.time(reminder)
        }));

        if (reminder.isRepeatable()) {
            result.append("\n").append(getNextRemindAt(nextRemindAt.withZoneSameInstant(reminder.getReceiverZoneId())));
        }

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result
                    .append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                            reminder.getNote()
                    }));
        }

        return result.toString();
    }
}
