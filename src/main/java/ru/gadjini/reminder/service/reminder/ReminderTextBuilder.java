package ru.gadjini.reminder.service.reminder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.util.UserUtils;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class ReminderTextBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private LocalisationService localisationService;

    @Autowired
    public ReminderTextBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String create(Reminder reminder) {
        return create0(reminder.getText(), reminder.getRemindAtInReceiverTimeZone(), reminder.getNote());
    }

    public String create(String text, ZonedDateTime remindAt, String note) {
        return create0(text, remindAt, note);
    }

    public String reminderCreatedReceiver(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_RECEIVER, new Object[]{
                reminder.getText() + " " + time(reminder.getRemindAtInReceiverTimeZone()),
                UserUtils.userLink(reminder.getCreator())
        }));

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    reminder.getNote()
            }));
        }

        return result.toString();
    }

    public String reminderCreatedCreator(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_CREATOR, new Object[]{
                reminder.getText() + " " + time(reminder.getRemindAtInReceiverTimeZone()),
                UserUtils.userLink(reminder.getReceiver())
        }));

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    reminder.getNote()
            }));
        }

        return result.toString();
    }

    public String reminderCreatedMe(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_CREATED_ME, new Object[]{
                reminder.getText() + " " + time(reminder.getRemindAtInReceiverTimeZone())
        }));

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result
                    .append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                            reminder.getNote()
                    }));
        }

        return result.toString();
    }

    public String remindReceiver(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND, new Object[]{
                reminder.getText() + " " + time(reminder.getRemindAtInReceiverTimeZone()),
                UserUtils.userLink(reminder.getCreator())
        }));

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    reminder.getNote()
            }));
        }

        return result.toString();
    }

    public String remindMe(Reminder reminder) {
        StringBuilder result = new StringBuilder();

        result.append(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_ME, new Object[]{
                reminder.getText() + " " + time(reminder.getRemindAtInReceiverTimeZone())
        }));

        if (StringUtils.isNotBlank(reminder.getNote())) {
            result
                    .append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                            reminder.getNote()
                    }));
        }

        return result.toString();
    }

    public String changeReminderTime(String text, TgUser creator, ZonedDateTime newRemindAt, ZonedDateTime oldRemindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_FROM, new Object[]{
                UserUtils.userLink(creator),
                text,
                time(oldRemindAt),
                time(newRemindAt)
        });
    }

    public String changeReminderNoteReceiver(String text, String note, TgUser creator, ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_EDITED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                time(remindAt),
                note
        });
    }

    public String deleteReminderNoteReceiver(String text, TgUser creator, ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE_DELETED_RECEIVER, new Object[]{
                UserUtils.userLink(creator),
                text,
                time(remindAt)
        });
    }

    public String postponeReminderTimeForCreator(String text, TgUser receiver, ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED_CREATOR, new Object[]{
                UserUtils.userLink(receiver),
                text,
                postponeTime(remindAt)
        });
    }

    public String remindersList(int requesterId, List<Reminder> reminders) {
        StringBuilder text = new StringBuilder();

        int i = 1;
        for (Reminder reminder : reminders) {
            String number = i++ + ") ";
            text.append(number).append(reminder.getText()).append("(").append(time(reminder.getRemindAtInReceiverTimeZone())).append(")\n");

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

    public String postponeReminderForReceiver(String text, ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED_RECEIVER, new Object[]{
                text,
                postponeTime(remindAt)
        });
    }

    public String changeReminderText(String oldText, String newText, TgUser creator) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED_FROM, new Object[]{
                UserUtils.userLink(creator),
                oldText,
                newText
        });
    }

    public String customRemindText(ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[]{
                time(remindAt)
        });
    }

    private String time(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt);

        if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_REMINDER_TODAY,
                    new Object[]{remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
            );
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
            return tomorrowTime(remindAt.getDayOfWeek(), time);
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
            return dayAfterTomorrowTime(now.getDayOfWeek(), time);
        } else {
            return fixedDay(remindAt, time);
        }
    }

    private String fixedDay(ZonedDateTime remindAt, String time) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_FIXED_DAY,
                new Object[]{remindAt.getDayOfMonth(), monthName, remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
        );
    }

    private String tomorrowTime(DayOfWeek dayOfWeek, String time) {
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_TOMORROW,
                new Object[]{dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
        );
    }

    private String dayAfterTomorrowTime(DayOfWeek dayOfWeek, String time) {
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW,
                new Object[]{dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
        );
    }

    private String postponeTime(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt) + "(" + remindAt.getDayOfWeek() + ")";

        if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{"<b>" + time + "</b>"});
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{tomorrowTime(remindAt.getDayOfWeek(), time)});
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{dayAfterTomorrowTime(remindAt.getDayOfWeek(), time)});
        } else {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{fixedDay(remindAt, time)});
        }
    }

    private String create0(String text, ZonedDateTime remindAt, String note) {
        StringBuilder result = new StringBuilder();

        result.append(text).append(" ").append(time(remindAt));

        if (StringUtils.isNotBlank(note)) {
            result.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTE, new Object[]{
                    note
            }));
        }

        return result.toString();
    }
}
