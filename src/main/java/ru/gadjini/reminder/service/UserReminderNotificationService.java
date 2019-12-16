package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.UserReminderNotificationDao;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.remind.parser.CustomRemindTime;
import ru.gadjini.reminder.service.security.SecurityService;

import java.time.ZoneId;
import java.util.List;

@Service
public class UserReminderNotificationService {

    private UserReminderNotificationDao dao;

    private TgUserService userService;

    private final LocalisationService localisationService;

    private SecurityService securityService;

    private RequestParser requestParser;

    @Autowired
    public UserReminderNotificationService(UserReminderNotificationDao dao, TgUserService userService,
                                           LocalisationService localisationService,
                                           SecurityService securityService, RequestParser requestParser) {
        this.dao = dao;
        this.userService = userService;
        this.localisationService = localisationService;
        this.securityService = securityService;
        this.requestParser = requestParser;
    }

    public void create(String text) {
        ZoneId zoneId = userService.getTimeZone(securityService.getAuthenticatedUser().getId());
        CustomRemindTime customRemindTime = parseCustomRemind(text, zoneId);
        UserReminderNotification userReminderNotification = new UserReminderNotification();
        userReminderNotification.setHours(customRemindTime.getOffsetTime().getHours());
        userReminderNotification.setMinutes(customRemindTime.getOffsetTime().getMinutes());
    }

    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType) {
        return dao.getList(userId, notificationType);
    }

    public void deleteById(int id) {
        dao.deleteById(id);
    }

    private CustomRemindTime parseCustomRemind(String text, ZoneId zoneId) {
        try {
            return requestParser.parseCustomRemind(text, zoneId);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_CUSTOM_REMIND));
        }
    }
}
