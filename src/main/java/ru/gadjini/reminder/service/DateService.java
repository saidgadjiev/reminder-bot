package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.*;

@Service
public class DateService {

    private TgUserService userService;

    private SecurityService securityService;

    @Autowired
    public DateService(TgUserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    public LocalDateTime currentUserDateToUtcDate(LocalTime localTime) {
        User user = securityService.getAuthenticatedUser();
        ZoneId zoneId = userService.getTimeZone(user.getId());

        return ZonedDateTime.of(LocalDate.now(zoneId), localTime, zoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public LocalDateTime toCurrentUserDate(LocalDateTime utcDateTime) {
        User user = securityService.getAuthenticatedUser();
        ZoneId zoneId = userService.getTimeZone(user.getId());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(utcDateTime, ZoneOffset.UTC);

        return zonedDateTime.withZoneSameInstant(zoneId).toLocalDateTime();
    }

    public LocalDateTime toUserDate(int userId, LocalDateTime utcDateTime) {
        ZoneId zoneId = userService.getTimeZone(userId);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(utcDateTime, ZoneOffset.UTC);

        return zonedDateTime.withZoneSameInstant(zoneId).toLocalDateTime();
    }
}
