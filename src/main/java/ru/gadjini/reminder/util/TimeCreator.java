package ru.gadjini.reminder.util;

import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.time.DateTime;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeCreator {

    public ZonedDateTime zonedDateTimeNow(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId).withSecond(0).withNano(0);
    }

    public LocalDateTime localDateTimeNow() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }

    public ZonedDateTime zonedDateTimeNow() {
        return ZonedDateTime.now().withSecond(0).withNano(0);
    }

    public LocalTime localTimeNow(ZoneId zoneId) {
        return LocalTime.now(zoneId).withSecond(0).withNano(0);
    }

    public LocalDate localDateNow(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    public LocalDate localDateNow() {
        return LocalDate.now();
    }

    public DateTime dateTimeNow(ZoneId zoneId) {
        return DateTime.of(zonedDateTimeNow(zoneId));
    }

    public List<RepeatTime> withZone(List<RepeatTime> repeatTimes, ZoneId target) {
        List<RepeatTime> resultList = new ArrayList<>();

        for (RepeatTime repeatTime : repeatTimes) {
            resultList.add(withZone(repeatTime, target));
        }

        return resultList;
    }

    public RepeatTime withZone(RepeatTime repeatTime, ZoneId target) {
        RepeatTime result = new RepeatTime(target);
        result.setDayOfWeek(repeatTime.getDayOfWeek());
        result.setInterval(repeatTime.getInterval());
        result.setDay(repeatTime.getDay());
        result.setMonth(repeatTime.getMonth());
        result.setSeriesToComplete(repeatTime.getSeriesToComplete());
        if (repeatTime.hasTime()) {
            LocalTime time = ZonedDateTime.of(localDateNow(repeatTime.getZoneId()), repeatTime.getTime(), repeatTime.getZoneId()).withZoneSameInstant(target).toLocalTime();
            result.setTime(time);
        }

        return result;
    }

    public OffsetTime withZone(OffsetTime offsetTime, ZoneId target) {
        OffsetTime result = new OffsetTime(target);
        result.setMinutes(offsetTime.getMinutes());
        result.setHours(offsetTime.getHours());
        result.setType(offsetTime.getType());
        result.setDays(offsetTime.getDays());
        if (offsetTime.getTime() != null) {
            LocalTime time = ZonedDateTime.of(localDateNow(offsetTime.getZoneId()), offsetTime.getTime(), offsetTime.getZoneId()).withZoneSameInstant(target).toLocalTime();
            result.setTime(time);
        }

        return result;
    }

    public UserReminderNotification withZone(UserReminderNotification notification, ZoneId target) {
        UserReminderNotification userReminderNotification = new UserReminderNotification(target);
        userReminderNotification.setMinutes(notification.getMinutes());
        userReminderNotification.setHours(notification.getHours());
        userReminderNotification.setUser(notification.getUser());
        userReminderNotification.setType(notification.getType());
        userReminderNotification.setDays(notification.getDays());
        if (notification.getTime() != null) {
            LocalTime time = ZonedDateTime.of(localDateNow(notification.getZoneId()), notification.getTime(), notification.getZoneId()).withZoneSameInstant(target).toLocalTime();
            userReminderNotification.setTime(time);
        }
        userReminderNotification.setUserId(notification.getUserId());

        return userReminderNotification;
    }

}
