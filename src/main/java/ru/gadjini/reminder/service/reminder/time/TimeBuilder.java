package ru.gadjini.reminder.service.reminder.time;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.service.declension.TimeDeclensionProvider;
import ru.gadjini.reminder.service.declension.TimeDeclensionService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Service
public class TimeBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private LocalisationService localisationService;

    private final TimeDeclensionProvider timeDeclensionProvider;

    private TimeCreator timeCreator;

    @Autowired
    public TimeBuilder(LocalisationService localisationService, TimeDeclensionProvider timeDeclensionProvider, TimeCreator timeCreator) {
        this.localisationService = localisationService;
        this.timeDeclensionProvider = timeDeclensionProvider;
        this.timeCreator = timeCreator;
    }

    public String deactivated(Locale locale) {
        return localisationService.getMessage(MessagesProperties.DEACTIVATED_TIME, locale);
    }

    public String time(OffsetTime offsetTime, Locale locale) {
        StringBuilder builder = new StringBuilder();

        builder.append("<b>");
        if (offsetTime.getDays() > 0 || offsetTime.getHours() > 0 || offsetTime.getMinutes() > 0) {
            String typeBefore = localisationService.getMessage(MessagesProperties.OFFSET_TIME_TYPE_BEFORE, locale);
            builder.append(typeBefore).append(" ");
        }
        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());

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
            String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
            builder.append(timeArticle).append(" ").append(DATE_TIME_FORMATTER.format(offsetTime.getTime()));
        }

        return builder.toString().trim() + "</b>";
    }

    public String time(List<RepeatTime> repeatTimes, Locale locale) {
        StringBuilder time = new StringBuilder();

        time.append("<b>");
        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());
        if (repeatTimes.get(0).getDayOfWeek() != null) {
            time.append(declensionService.getRepeatWord(repeatTimes.get(0).getDayOfWeek())).append(" ");
        } else {
            time.append(declensionService.getRepeatWord(repeatTimes.get(0).getInterval())).append(" ");
        }
        for (Iterator<RepeatTime> iterator = repeatTimes.iterator(); iterator.hasNext();) {
            time.append(getRepeatTimeView(iterator.next(), locale));
            if (iterator.hasNext()) {
                time.append(", ");
            }
        }
        time.append("</b>");

        return time.toString();
    }

    public String time(RepeatTime repeatTime, Locale locale) {
        StringBuilder time = new StringBuilder();

        time.append("<b>");
        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());
        if (repeatTime.getDayOfWeek() != null) {
            time.append(declensionService.getRepeatWord(repeatTime.getDayOfWeek())).append(" ");
        } else {
            time.append(declensionService.getRepeatWord(repeatTime.getInterval())).append(" ");
        }
        time.append(getRepeatTimeView(repeatTime, locale));
        time.append("</b>");

        return time.toString();
    }

    public String time(DateTime dateTime, Locale locale) {
        if (!dateTime.hasTime()) {
            return time(dateTime.date(), dateTime.getZoneId(), locale);
        }

        return time(dateTime.toZonedDateTime(), locale);
    }

    public String time(LocalDate remindAt, ZoneId zoneId, Locale locale) {
        LocalDate now = timeCreator.localDateNow(zoneId);

        if (remindAt.getMonth().equals(now.getMonth()) && remindAt.getYear() == now.getYear()) {
            if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
                return todayDate(remindAt, locale);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
                return tomorrowDate(remindAt, locale);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
                return dayAfterTomorrowDate(remindAt, locale);
            }
        }

        return fixedDate(remindAt, zoneId, locale);
    }

    public String time(ZonedDateTime remindAt, Locale locale) {
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZone());

        if (remindAt.getMonth().equals(now.getMonth()) && remindAt.getYear() == now.getYear()) {
            if (remindAt.getDayOfMonth() == now.getDayOfMonth()) {
                return todayTime(remindAt, locale);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 1) {
                return tomorrowTime(remindAt, locale);
            } else if (remindAt.getDayOfMonth() - now.getDayOfMonth() == 2) {
                return dayAfterTomorrowTime(remindAt, locale);
            }
        }

        return fixedDay(remindAt, locale);
    }

    public String time(Period period, Locale locale) {
        return time(period, true, locale);
    }

    public String fixedDay(ZonedDateTime remindAt, Locale locale) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, locale);
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
        ZonedDateTime now = timeCreator.zonedDateTimeNow(remindAt.getZone());

        return "<b>" + remindAt.getDayOfMonth() + " " + monthName +
                "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ") " +
                timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + (now.getYear() < remindAt.getYear() ? " " + remindAt.getYear() : "") + "</b>";
    }

    public String time(Period period, boolean bold, Locale locale) {
        StringBuilder time = new StringBuilder();

        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());
        if (bold) {
            time.append("<b>");
        }
        time.append(declensionService.getRepeatWord(period)).append(" ");
        time.append(getPeriodView(period, locale));

        return time.toString().trim() + (bold ? "</b>" : "");
    }

    private String todayDate(LocalDate remindAt, Locale locale) {
        String today = localisationService.getMessage(MessagesProperties.TODAY, locale);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ")</b>";
    }

    private String tomorrowDate(LocalDate remindAt, Locale locale) {
        String today = localisationService.getMessage(MessagesProperties.TOMORROW, locale);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ")</b>";
    }

    private String dayAfterTomorrowDate(LocalDate remindAt, Locale locale) {
        String today = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW, locale);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ")</b>";
    }

    private String fixedDate(LocalDate remindAt, ZoneId zoneId, Locale locale) {
        String monthName = remindAt.getMonth().getDisplayName(TextStyle.FULL, locale);
        LocalDate now = timeCreator.localDateNow(zoneId);

        return "<b>" + remindAt.getDayOfMonth() + " " + monthName +
                "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ")"
                + (now.getYear() < remindAt.getYear() ? " " + remindAt.getYear() : "") + "</b>";
    }

    private String todayTime(ZonedDateTime remindAt, Locale locale) {
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
        String today = localisationService.getMessage(MessagesProperties.TODAY, locale);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    private String tomorrowTime(ZonedDateTime remindAt, Locale locale) {
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
        String today = localisationService.getMessage(MessagesProperties.TOMORROW, locale);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    private String dayAfterTomorrowTime(ZonedDateTime remindAt, Locale locale) {
        String timeArticle = localisationService.getMessage(MessagesProperties.TIME_ARTICLE, locale);
        String today = localisationService.getMessage(MessagesProperties.DAY_AFTER_TOMORROW, locale);

        return "<b>" + today + "(" + remindAt.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale) + ") " + timeArticle + " " + DATE_TIME_FORMATTER.format(remindAt) + "</b>";
    }

    private String getRepeatTimeView(RepeatTime repeatTime, Locale locale) {
        StringBuilder time = new StringBuilder();

        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());
        if (repeatTime.getDayOfWeek() != null) {
            time.append(declensionService.dayOfWeek(repeatTime.getDayOfWeek())).append(" ");
            if (repeatTime.getTime() != null) {
                time.append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        } else if (repeatTime.getInterval().getMonths() != 0) {
            time.append(getPeriodView(repeatTime.getInterval(), locale)).append(" ");
            time.append(repeatTime.getDay()).append(" ").append(localisationService.getMessage(MessagesProperties.REGEXP_MONTH_DAY_PREFIX, locale));
            if (repeatTime.getTime() != null) {
                time.append(" ").append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        } else if (repeatTime.getInterval().getYears() != 0) {
            time.append(getPeriodView(repeatTime.getInterval(), locale)).append(" ");
            time.append(repeatTime.getDay()).append(" ").append(repeatTime.getMonth().getDisplayName(TextStyle.FULL, locale));
            if (repeatTime.getTime() != null) {
                time.append(" ").append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        } else if (repeatTime.getInterval() != null) {
            time.append(getPeriodView(repeatTime.getInterval(), locale));
            if (repeatTime.getTime() != null) {
                time.append(" ").append(DATE_TIME_FORMATTER.format(repeatTime.getTime()));
            }
        }

        return time.toString().trim();
    }

    private String getPeriodView(Period period, Locale locale) {
        StringBuilder time = new StringBuilder();

        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());
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

        return time.toString().trim();
    }
}
