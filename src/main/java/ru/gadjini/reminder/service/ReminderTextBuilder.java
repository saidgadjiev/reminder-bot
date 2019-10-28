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

    private String time(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt);

        if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TODAY, new Object[] { time });
        } else if (now.getDayOfMonth() - remindAt.getDayOfMonth() == 1) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_TOMORROW, new Object[] { time });
        } else if (now.getDayOfMonth() - remindAt.getDayOfMonth() == 2) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW, new Object[] { time });
        } else {
            String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FIXED_DAY, new Object[] { remindAt.getDayOfMonth(), monthName, time });
        }
    }
}
