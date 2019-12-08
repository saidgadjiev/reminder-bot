package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.service.message.LocalisationService;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class TimeBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private LocalisationService localisationService;

    @Autowired
    public TimeBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String time(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt);

        if (remindAt.getMonth().equals(now.getMonth()) && remindAt.getYear() == now.getYear()) {
            if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_REMINDER_TODAY,
                        new Object[]{remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
                );
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
                return tomorrowTime(remindAt.getDayOfWeek(), time);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
                return dayAfterTomorrowTime(now.getDayOfWeek(), time);
            }
        }

        return fixedDay(remindAt, time);
    }

    public String fixedDay(ZonedDateTime remindAt, String time) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_FIXED_DAY,
                new Object[]{remindAt.getDayOfMonth(), monthName, remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
        );
    }

    public String tomorrowTime(DayOfWeek dayOfWeek, String time) {
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_TOMORROW,
                new Object[]{dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
        );
    }

    public String dayAfterTomorrowTime(DayOfWeek dayOfWeek, String time) {
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW,
                new Object[]{dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), time}
        );
    }

    public String postponeTime(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt) + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")";

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

    public String time(Reminder reminder) {
        if (reminder.isRepeatable()) {
            return time(reminder.getRepeatRemindAt());
        } else {
            return time(reminder.getRemindAtInReceiverTimeZone());
        }
    }

    public String time(RepeatTime repeatTime) {
        StringBuilder time = new StringBuilder("каждые ");

        if (repeatTime.getInterval() != null) {
            time.append(time(repeatTime.getInterval()));
        } else {
            time.append(repeatTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())).append(" ");
            time.append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
        }

        return time.toString();
    }

    public String time(Period period) {
        StringBuilder time = new StringBuilder();

        if (period.getDays() != 0) {
            time.append(period.getDays()).append(" дней ");
        }
        if (period.getHours() != 0) {
            time.append(period.getHours()).append(" часов ");
        }
        if (period.getMinutes() != 0) {
            time.append(period.getMinutes()).append(" минут ");
        }

        return time.toString();
    }
}
