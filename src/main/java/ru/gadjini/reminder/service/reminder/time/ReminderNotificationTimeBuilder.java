package ru.gadjini.reminder.service.reminder.time;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.RepeatTime;

import java.time.ZonedDateTime;
import java.util.Locale;

@Service
public class ReminderNotificationTimeBuilder {

    private TimeBuilder timeBuilder;

    @Autowired
    public ReminderNotificationTimeBuilder(TimeBuilder timeBuilder) {
        this.timeBuilder = timeBuilder;
    }

    public String time(ReminderNotification reminderNotification, Locale locale) {
        if (reminderNotification.getType().equals(ReminderNotification.Type.ONCE)) {
            return timeBuilder.time(reminderNotification.getFixedTime().withZoneSameInstant(reminderNotification.getReminder().getReceiver().getZone()), locale);
        }

        RepeatTime repeatTime = new RepeatTime(reminderNotification.getLastReminderAt().getZone());
        ZonedDateTime lastRemindAt = reminderNotification.getLastReminderAt().withZoneSameInstant(reminderNotification.getReminder().getReceiver().getZone());
        if (reminderNotification.getDelayTime().getDays() == 7) {
            repeatTime.setDayOfWeek(lastRemindAt.getDayOfWeek());
            repeatTime.setTime(lastRemindAt.toLocalTime());
        } else if (reminderNotification.getDelayTime().getDays() != 0) {
            repeatTime.setInterval(reminderNotification.getDelayTime());
            repeatTime.setTime(lastRemindAt.toLocalTime());
        } else if (reminderNotification.getDelayTime().getMonths() != 0) {
            repeatTime.setInterval(reminderNotification.getDelayTime());
            repeatTime.setDay(lastRemindAt.getDayOfMonth());
            repeatTime.setTime(lastRemindAt.toLocalTime());
        } else if (reminderNotification.getDelayTime().getYears() != 0) {
            repeatTime.setInterval(reminderNotification.getDelayTime());
            repeatTime.setMonth(lastRemindAt.getMonth());
            repeatTime.setDay(lastRemindAt.getDayOfMonth());
            repeatTime.setTime(lastRemindAt.toLocalTime());
        } else {
            return timeBuilder.time(reminderNotification.getDelayTime(), locale);
        }

        return timeBuilder.time(repeatTime, locale);
    }

    public String time(UserReminderNotification userReminderNotification) {
        OffsetTime offsetTime = new OffsetTime(userReminderNotification.getZoneId());
        offsetTime.setTime(userReminderNotification.getTime());
        offsetTime.setType(OffsetTime.Type.BEFORE);
        offsetTime.setPeriod(new Period().withDays(userReminderNotification.getDays()).withHours(userReminderNotification.getHours()).withMinutes(userReminderNotification.getMinutes()));

        return timeBuilder.time(offsetTime, userReminderNotification.getUser().getLocale());
    }
}
