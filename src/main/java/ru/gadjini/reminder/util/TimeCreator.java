package ru.gadjini.reminder.util;

import org.springframework.stereotype.Service;
import ru.gadjini.reminder.time.DateTime;

import java.time.*;

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
}
