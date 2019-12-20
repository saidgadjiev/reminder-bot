package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.UserReminderNotificationDao;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.domain.Time;
import ru.gadjini.reminder.service.security.SecurityService;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class UserReminderNotificationService {

    private UserReminderNotificationDao dao;

    private TgUserService userService;

    private final LocalisationService localisationService;

    private SecurityService securityService;

    private RequestParser requestParser;

    @Autowired
    public UserReminderNotificationService(UserReminderNotificationDao dao,
                                           LocalisationService localisationService,
                                           SecurityService securityService, RequestParser requestParser) {
        this.dao = dao;
        this.localisationService = localisationService;
        this.securityService = securityService;
        this.requestParser = requestParser;
    }

    @Autowired
    public void setUserService(TgUserService userService) {
        this.userService = userService;
    }

    public void create(String text, UserReminderNotification.NotificationType notificationType) {
        User user = securityService.getAuthenticatedUser();
        ZoneId zoneId = userService.getTimeZone(user.getId());

        Time time = parseCustomRemind(text, zoneId);
        time.setOffsetTime(time.getOffsetTime().withZone(ZoneOffset.UTC));
        UserReminderNotification userReminderNotification = new UserReminderNotification(ZoneOffset.UTC);
        userReminderNotification.setDays(time.getOffsetTime().getDays());
        userReminderNotification.setTime(time.getOffsetTime().getTime());
        userReminderNotification.setHours(time.getOffsetTime().getHours());
        userReminderNotification.setMinutes(time.getOffsetTime().getMinutes());
        userReminderNotification.setUserId(user.getId());
        userReminderNotification.setType(notificationType);

        dao.create(userReminderNotification);
    }

    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType) {
        return dao.getList(userId, notificationType);
    }

    public void deleteById(int id) {
        dao.deleteById(id);
    }

    public int count(int userId, UserReminderNotification.NotificationType notificationType) {
        return dao.count(userId, notificationType);
    }

    public void createDefaultNotificationsForWithTime(int userId) {
        UserReminderNotification eveNotification = new UserReminderNotification(ZoneOffset.UTC);
        eveNotification.setType(UserReminderNotification.NotificationType.WITH_TIME);
        eveNotification.setUserId(userId);
        eveNotification.setDays(1);
        eveNotification.setTime(LocalTime.of(19, 0));
        dao.create(eveNotification);

        dao.create(buildOffsetNotification(userId, 2, 0));
        dao.create(buildOffsetNotification(userId, 1, 0));
        dao.create(buildOffsetNotification(userId, 0, 20));
    }

    public void createDefaultNotificationsForWithoutTime(int userId) {
        UserReminderNotification noonNotification = new UserReminderNotification(ZoneOffset.UTC);
        noonNotification.setType(UserReminderNotification.NotificationType.WITHOUT_TIME);
        noonNotification.setTime(LocalTime.of(9, 0));
        noonNotification.setUserId(userId);
        dao.create(noonNotification);

        UserReminderNotification eveningNotification = new UserReminderNotification(ZoneOffset.UTC);
        eveningNotification.setType(UserReminderNotification.NotificationType.WITHOUT_TIME);
        eveningNotification.setTime(LocalTime.of(19, 0));
        eveningNotification.setUserId(userId);
        dao.create(eveningNotification);

        UserReminderNotification eveNotification = new UserReminderNotification(ZoneOffset.UTC);
        eveNotification.setType(UserReminderNotification.NotificationType.WITHOUT_TIME);
        eveNotification.setTime(LocalTime.of(19, 0));
        eveNotification.setDays(1);
        eveNotification.setUserId(userId);
        dao.create(eveNotification);
    }
    private UserReminderNotification buildOffsetNotification(int userId, int hours, int minutes) {
        UserReminderNotification notification = new UserReminderNotification(ZoneOffset.UTC);
        notification.setType(UserReminderNotification.NotificationType.WITH_TIME);
        notification.setUserId(userId);
        notification.setHours(hours);
        notification.setMinutes(minutes);

        return notification;
    }

    private Time parseCustomRemind(String text, ZoneId zoneId) {
        try {
            return requestParser.parseCustomRemind(text, zoneId);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND));
        }
    }
}
