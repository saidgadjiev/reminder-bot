package ru.gadjini.reminder.service.reminder.time;

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

    private IntervalLocalisationService intervalLocalisationService;

    @Autowired
    public TimeBuilder(LocalisationService localisationService, IntervalLocalisationService intervalLocalisationService) {
        this.localisationService = localisationService;
        this.intervalLocalisationService = intervalLocalisationService;
    }

    public String time(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        if (remindAt.getMonth().equals(now.getMonth()) && remindAt.getYear() == now.getYear()) {
            if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
                return todayTime(remindAt);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
                return tomorrowTime(remindAt);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
                return dayAfterTomorrowTime(remindAt);
            }
        }

        return fixedDay(remindAt);
    }

    private String todayTime(ZonedDateTime remindAt) {
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_TODAY,
                new Object[]{remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), DATE_TIME_FORMATTER.format(remindAt)}
        );
    }

    private String fixedDay(ZonedDateTime remindAt) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_FIXED_DAY,
                new Object[]{remindAt.getDayOfMonth(), monthName, remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), DATE_TIME_FORMATTER.format(remindAt)}
        );
    }

    private String tomorrowTime(ZonedDateTime remindAt) {
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_TOMORROW,
                new Object[]{remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), DATE_TIME_FORMATTER.format(remindAt)}
        );
    }

    private String dayAfterTomorrowTime(ZonedDateTime remindAt) {
        return localisationService.getMessage(
                MessagesProperties.MESSAGE_REMINDER_DAY_AFTER_TOMORROW,
                new Object[]{remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()), DATE_TIME_FORMATTER.format(remindAt)}
        );
    }

    public String postponeTime(ZonedDateTime remindAt) {
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());
        String time = DATE_TIME_FORMATTER.format(remindAt) + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")";

        if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{time});
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{tomorrowTime(remindAt)});
        } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{dayAfterTomorrowTime(remindAt)});
        } else {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{fixedDay(remindAt)});
        }
    }

    public String time(Reminder reminder) {
        if (reminder.isRepeatable()) {
            return time(reminder.getRepeatRemindAt());
        } else {
            return time(reminder.getRemindAtInReceiverZone());
        }
    }

    public String time(RepeatTime repeatTime) {
        StringBuilder time = new StringBuilder();

        if (repeatTime.getInterval() != null) {
            time.append(intervalLocalisationService.get(repeatTime.getInterval()));
            if (repeatTime.getTime() != null) {
                time.append(" ").append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        } else {
            time.append(getRepeatWord(repeatTime.getDayOfWeek())).append(" ");
            time.append(repeatTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())).append(" ");
            time.append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
        }

        return time.toString();
    }

    private String getRepeatWord(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case THURSDAY:
                return "каждый";
            case WEDNESDAY:
            case FRIDAY:
            case SATURDAY:
                return "каждую";
            case SUNDAY:
                return "каждое";
        }

        throw new UnsupportedOperationException();
    }
}
