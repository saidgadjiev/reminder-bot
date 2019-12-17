package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.TgUserDao;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.domain.UserReminderNotification;

import java.time.ZoneId;

@Service
public class TgUserService {

    private TgUserDao tgUserDao;

    private UserReminderNotificationService userReminderNotificationService;

    @Autowired
    public TgUserService(TgUserDao tgUserDao) {
        this.tgUserDao = tgUserDao;
    }

    @Autowired
    public void setUserReminderNotificationService(UserReminderNotificationService userReminderNotificationService) {
        this.userReminderNotificationService = userReminderNotificationService;
    }

    public TgUser getByUserId(int userId) {
        return tgUserDao.getByUserId(userId);
    }

    public void createOrUpdateUser(long chatId, User user) {
        TgUser tgUser = new TgUser();

        tgUser.setUserId(user.getId());
        tgUser.setChatId(chatId);
        tgUser.setUsername(user.getUserName());
        tgUser.setFirstName(user.getFirstName());
        tgUser.setLastName(user.getLastName());

        tgUserDao.createOrUpdate(tgUser);
        createUserNotifications(user.getId());
    }

    public ZoneId getTimeZone(int userId) {
        return ZoneId.of("Europe/Moscow");
    }

    public ZoneId getTimeZone(String username) {
        return ZoneId.of("Europe/Moscow");
    }

    public void saveZoneId(int userId, ZoneId zoneId) {
        tgUserDao.updateTimezone(userId, zoneId);
    }

    private void createUserNotifications(int userId) {
        int countWithTime = userReminderNotificationService.count(userId, UserReminderNotification.NotificationType.WITH_TIME);
        if (countWithTime == 0) {
            userReminderNotificationService.createDefaultNotificationsForWithTime(userId);
        }

        int countWithoutTime = userReminderNotificationService.count(userId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        if (countWithoutTime == 0) {
            userReminderNotificationService.createDefaultNotificationsForWithoutTime(userId);
        }
    }
}
