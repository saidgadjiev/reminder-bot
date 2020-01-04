package ru.gadjini.reminder.dao.usernotification;

import ru.gadjini.reminder.domain.UserReminderNotification;

import java.util.List;

public interface UserReminderNotificationDao {
    void deleteById(int id);

    void create(UserReminderNotification userReminderNotification);

    int count(int userId, UserReminderNotification.NotificationType notificationType);

    List<UserReminderNotification> getList(int userId, UserReminderNotification.NotificationType notificationType);
}
