package ru.gadjini.reminder.service.reminder.time;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.service.declension.TimeDeclensionService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class TimeBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private LocalisationService localisationService;

    private Map<String, TimeDeclensionService> declensionServiceMap = new HashMap<>();

    @Autowired
    public TimeBuilder(LocalisationService localisationService, Collection<TimeDeclensionService> declensionServices) {
        this.localisationService = localisationService;
        declensionServices.forEach(timeDeclensionService -> declensionServiceMap.put(timeDeclensionService.getLanguage(), timeDeclensionService));
    }

    public String deactivated() {
        return localisationService.getMessage(MessagesProperties.DEACTIVATED_TIME);
    }

    public String time(OffsetTime offsetTime) {
        StringBuilder builder = new StringBuilder();

        builder.append("<b>");
        if (offsetTime.getDays() > 0 || offsetTime.getHours() > 0 || offsetTime.getMinutes() > 0) {
            String typeBefore = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_BEFORE);
            builder.append(typeBefore).append(" ");
        }
        TimeDeclensionService declensionService = declensionServiceMap.get(Locale.getDefault().getLanguage());

        if (offsetTime.getDays() != 0) {
            builder.append(declensionService.day(offsetTime.getDays())).append(" ");
        }
        if (offsetTime.getHours() != 0) {
            builder.append(declensionService.hour(offsetTime.getHours())).append(" ");
        }
        if (offsetTime.getMinutes() != 0) {
            builder.append(declensionService.minute(offsetTime.getMinutes())).append(" ");
        }
        if (offsetTime.getTime() != null) {
            String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
            builder.append(timeArticle).append(" ").append(DATE_TIME_FORMATTER.format(offsetTime.getTime()));
        }
        builder.append("</b>");

        return builder.toString().trim();
    }

    public String time(RepeatTime repeatTime) {
        StringBuilder time = new StringBuilder();

        time.append("<b>");
        TimeDeclensionService declensionService = declensionServiceMap.get(Locale.getDefault().getLanguage());
        if (repeatTime.getDayOfWeek() != null) {
            time.append(declensionService.getRepeatWord(repeatTime.getDayOfWeek())).append(" ");
            time.append(declensionService.dayOfWeek(repeatTime.getDayOfWeek())).append(" ");
            if (repeatTime.getTime() != null) {
                time.append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        } else if (repeatTime.getInterval().getMonths() != 0) {
            time.append(time(repeatTime.getInterval())).append(" ");
            time.append(repeatTime.getDay()).append(" ").append(localisationService.getMessage(MessagesProperties.REGEXP_MONTH_DAY_PREFIX));
            if (repeatTime.getTime() != null) {
                time.append(" ").append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        } else if (repeatTime.getInterval().getYears() != 0) {
            time.append(time(repeatTime.getInterval())).append(" ");
            time.append(repeatTime.getDay()).append(" ").append(repeatTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
            if (repeatTime.getTime() != null) {
                time.append(" ").append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        } else if (repeatTime.getInterval() != null) {
            time.append(time(repeatTime.getInterval()));
            if (repeatTime.getTime() != null) {
                time.append(" ").append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        }
        time.append("</b>");

        return time.toString();
    }

    public String time(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            return time(dateTime.date(), dateTime.getZoneId());
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

        return fixedDate(remindAt, zoneId);
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

    public String time(Period period) {
        StringBuilder time = new StringBuilder();

        TimeDeclensionService declensionService = declensionServiceMap.get(Locale.getDefault().getLanguage());
        time.append("<b>").append(declensionService.getRepeatWord(period)).append(" ");
        if (period.getYears() != 0) {
            time.append(declensionService.year(period.getYears())).append(" ");
        }
        if (period.getMonths() != 0) {
            time.append(declensionService.months(period.getMonths())).append(" ");
        }
        if (period.getDays() != 0) {
            time.append(declensionService.day(period.getDays())).append(" ");
        }
        if (period.getHours() != 0) {
            time.append(declensionService.hour(period.getHours())).append(" ");
        }
        if (period.getMinutes() != 0) {
            time.append(declensionService.minute(period.getMinutes())).append(" ");
        }
        time.append("</b>");

        return time.toString().trim();
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

    private String fixedDate(LocalDate remindAt, ZoneId zoneId) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        LocalDate now = LocalDate.now(zoneId);

        return "<b>"+ remindAt.getDayOfMonth() + " " + monthName +
                "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")"
                + (now.getYear() < remindAt.getYear() ? " " + remindAt.getYear() : "") + "</b>";
    }

    private String todayTime(ZonedDateTime remindAt) {
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        String today = localisationService.getMessage(MessagesProperties.TODAY);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    private String fixedDay(ZonedDateTime remindAt) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE);
        ZonedDateTime now = ZonedDateTime.now(remindAt.getZone());

        return "<b>" + remindAt.getDayOfMonth() + " " + monthName +
                "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ") " +
                timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + (now.getYear() < remindAt.getYear() ? " " + remindAt.getYear() : "") + "</b>";
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
}
