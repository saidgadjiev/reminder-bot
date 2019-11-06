package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class ReminderTextBuilder {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private LocalisationService localisationService;

    @Autowired
    public ReminderTextBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String create(String text, ZonedDateTime remindAt) {
        return text + " " + time(remindAt);
    }

    public String changeReminderTime(String text, TgUser creator, ZonedDateTime newRemindAt, ZonedDateTime oldRemindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TIME_EDITED_FROM, new Object[] {
                UserUtils.userLink(creator),
                text,
                time(oldRemindAt),
                time(newRemindAt)
        });
    }

    public String postponeReminderTimeForReceiver(String text, TgUser creator, ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED_FROM, new Object[] {
                text,
                UserUtils.userLink(creator),
                postponeTime(remindAt)
        });
    }

    public String postponeReminderTimeForCreator(String text, TgUser receiver, ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED, new Object[] {
                UserUtils.userLink(receiver),
                text,
                postponeTime(remindAt)
        });
    }

    public String postponeReminderForMe(String text, ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONED_ME, new Object[] {
                text,
                postponeTime(remindAt)
        });
    }

    public String changeReminderText(String oldText, String newText, TgUser creator) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TEXT_EDITED_FROM, new Object[] {
                UserUtils.userLink(creator),
                oldText,
                newText
        });
    }

    public String customRemindText(ZonedDateTime remindAt) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND_CREATED, new Object[] {
                time(remindAt)
        });
    }

    private String time(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt);

        if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TODAY, new Object[] { time });
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
            return tomorrowTime(time);
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
            return dayAfterTomorrowTime(time);
        } else {
            return fixedDay(remindAt, time);
        }
    }

    private String fixedDay(ZonedDateTime remindAt, String time) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FIXED_DAY, new Object[] { remindAt.getDayOfMonth(), monthName, time });
    }

    private String tomorrowTime(String time) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TOMORROW, new Object[] { time });
    }

    private String dayAfterTomorrowTime(String time) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW, new Object[] { time });
    }

    private String postponeTime(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt);

        if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[] { time });
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[] { tomorrowTime(time) });
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[] { dayAfterTomorrowTime(time) });
        } else {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[] { fixedDay(remindAt, time) });
        }
    }
}
