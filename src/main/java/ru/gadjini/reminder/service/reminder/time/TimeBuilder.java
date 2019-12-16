package ru.gadjini.reminder.service.reminder.time;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.domain.RepeatTime;
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

    public String time(ReminderTime reminderTime) {
        if (reminderTime.getType().equals(ReminderTime.Type.ONCE)) {
            return time(reminderTime.getFixedTime().withZoneSameInstant(reminderTime.getReminder().getReceiver().getZone()));
        }

        StringBuilder time = new StringBuilder();
        if (reminderTime.getDelayTime().getDays() == 7) {
            ZonedDateTime lastRemindAt = reminderTime.getLastReminderAt().withZoneSameInstant(reminderTime.getReminder().getReceiver().getZone());
            DayOfWeek dayOfWeek = lastRemindAt.getDayOfWeek();

            time.append(getRepeatWord(dayOfWeek)).append(" ");
            time.append(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())).append(" ");
            time.append(DATE_TIME_FORMATTER.format(lastRemindAt));
        } else {
            time.append(intervalLocalisationService.get(reminderTime.getDelayTime()));
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

        return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_POSTPONE_TIME, new Object[] {time});
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
