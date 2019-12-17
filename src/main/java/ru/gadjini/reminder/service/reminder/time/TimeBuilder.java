package ru.gadjini.reminder.service.reminder.time;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.RepeatTime;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
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

    public String time(UserReminderNotification offsetTime) {
        String typeBefore = localisationService.getMessage(MessagesProperties.CUSTOM_REMIND_BEFORE);
        StringBuilder builder = new StringBuilder(typeBefore).append(" ");

        if (offsetTime.getDays() != 0) {
            builder.append(offsetTime.getDays()).append(" дней ");
        }
        if (offsetTime.getHours() != 0) {
            builder.append(offsetTime.getHours()).append(" часа ");
        }
        if (offsetTime.getMinutes() != 0) {
            builder.append(offsetTime.getMinutes()).append(" минут ");
        }
        if (offsetTime.getTime() != null) {
            builder.append("в ").append(DATE_TIME_FORMATTER.format(offsetTime.getTime()));
        }

        return builder.toString().trim();
    }

    public String time(ReminderNotification reminderNotification) {
        if (reminderNotification.getType().equals(ReminderNotification.Type.ONCE)) {
            return time(reminderNotification.getFixedTime().withZoneSameInstant(reminderNotification.getReminder().getReceiver().getZone()));
        }

        StringBuilder time = new StringBuilder();
        if (reminderNotification.getDelayTime().getDays() == 7) {
            ZonedDateTime lastRemindAt = reminderNotification.getLastReminderAt().withZoneSameInstant(reminderNotification.getReminder().getReceiver().getZone());
            DayOfWeek dayOfWeek = lastRemindAt.getDayOfWeek();

            time.append(getRepeatWord(dayOfWeek)).append(" ");
            time.append(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())).append(" ");
            time.append(DATE_TIME_FORMATTER.format(lastRemindAt));
        } else if (reminderNotification.getDelayTime().getDays() != 0) {
            time.append(getRepeatWord(reminderNotification.getDelayTime())).append(" ");
            time.append(DATE_TIME_FORMATTER.format(reminderNotification.getLastReminderAt()));
        } else {
            time.append(intervalLocalisationService.get(reminderNotification.getDelayTime()));
        }

        return time.toString();
    }

    public String time(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            return time(dateTime.date(), dateTime.getZone());
        }

        return time(dateTime.toZonedDateTime());
    }

    public String time(LocalDate remindAt, ZoneId zoneId) {
        LocalDate now = LocalDate.now(zoneId);

        if (remindAt.getMonth().equals(now.getMonth()) && remindAt.getYear() == now.getYear()) {
            if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
                return todayDate(remindAt);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
                return tomorrowDate(remindAt);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
                return dayAfterTomorrowDate(remindAt);
            }
        }

        return fixedDate(remindAt);
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

    private String todayDate(LocalDate remindAt) {
        String today = localisationService.getMessage(MessagesProperties.TODAY);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")</b>";
    }

    private String tomorrowDate(LocalDate remindAt) {
        String today = localisationService.getMessage(MessagesProperties.TOMORROW);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")</b>";
    }

    private String dayAfterTomorrowDate(LocalDate remindAt) {
        String today = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")</b>";
    }

    private String fixedDate(LocalDate remindAt) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        return "<b>" + remindAt.getDayOfMonth() + " " + monthName + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")</b>";
    }

    private String todayTime(ZonedDateTime remindAt) {
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        String today = localisationService.getMessage(MessagesProperties.TODAY);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    private String fixedDay(ZonedDateTime remindAt) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);

        return "<b>" + remindAt.getDayOfMonth() + " " + monthName + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    private String tomorrowTime(ZonedDateTime remindAt) {
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        String today = localisationService.getMessage(MessagesProperties.TOMORROW);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    private String dayAfterTomorrowTime(ZonedDateTime remindAt) {
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        String today = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    public String postponeTime(DateTime remindAt) {
        String time = time(remindAt);

        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[]{time});
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

    private String getRepeatWord(Period period) {
        if (period.getHours() == 1) {
            return "каждый";
        }
        if (period.getMinutes() == 1) {
            return "каждую";
        }

        return "каждые";
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
