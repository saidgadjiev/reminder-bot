package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.usernotification.UserReminderNotificationDao;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.validation.ValidationContext;
import ru.gadjini.reminder.service.validation.ValidatorFactory;
import ru.gadjini.reminder.service.validation.ValidatorType;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class UserReminderNotificationService {

    private UserReminderNotificationDao dao;

    private TgUserService userService;

    private final LocalisationService localisationService;

    private RequestParser requestParser;

    private ValidatorFactory validatorFactory;

    private TimeCreator timeCreator;

    @Autowired
    public UserReminderNotificationService(@Qualifier("redis") UserReminderNotificationDao dao,
                                           LocalisationService localisationService,
                                           RequestParser requestParser, TimeCreator timeCreator) {
        this.dao = dao;
        this.localisationService = localisationService;
        this.requestParser = requestParser;
        this.timeCreator = timeCreator;
    }

    @Autowired
    public void setValidatorFactory(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Autowired
    public void setUserService(TgUserService userService) {
        this.userService = userService;
    }

    public void create(int userId, String text, UserReminderNotification.NotificationType notificationType) {
        ZoneId zoneId = userService.getTimeZone(userId);

        Time time = parseCustomRemind(text, zoneId);
        validatorFactory.getValidator(ValidatorType.USER_REMINDER_NOTIFICATION).validate(new ValidationContext().time(time));

        time.setOffsetTime(timeCreator.withZone(time.getOffsetTime(), ZoneOffset.UTC));
        UserReminderNotification userReminderNotification = new UserReminderNotification(ZoneOffset.UTC);
        userReminderNotification.setDays(time.getOffsetTime().getDays());
        userReminderNotification.setTime(time.getOffsetTime().getTime());
        userReminderNotification.setHours(time.getOffsetTime().getHours());
        userReminderNotification.setMinutes(time.getOffsetTime().getMinutes());
        userReminderNotification.setUserId(userId);
        userReminderNotification.setType(notificationType);

        dao.create(userReminderNotification);
    }

    public List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType) {
        return dao.getList(userId, notificationType, true);
    }

    public List<UserReminderNotification> getNonCachedList(int userId, UserReminderNotification.NotificationType notificationType) {
        return dao.getList(userId, notificationType, false);
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
            return requestParser.parseTime(text, zoneId);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_USER_REMIND));
        }
    }
}
