package ru.gadjini.reminder.service.reminder.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.ReminderNotificationDao;
import ru.gadjini.reminder.domain.ReminderNotification;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderNotificationService {

    private ReminderNotificationDao reminderNotificationDao;

    @Autowired
    public ReminderNotificationService(ReminderNotificationDao reminderNotificationDao) {
        this.reminderNotificationDao = reminderNotificationDao;
    }

    public void create(ReminderNotification reminderNotification) {
        reminderNotificationDao.create(reminderNotification);
    }

    public void create(List<ReminderNotification> reminderNotifications) {
        reminderNotificationDao.create(reminderNotifications);
    }

    public void deleteReminderTimes(int reminderId) {
        reminderNotificationDao.deleteByReminderId(reminderId);
    }

    public int deleteReminderTime(int id) {
        return reminderNotificationDao.delete(id);
    }

    public void updateLastRemindAt(int id, LocalDateTime lastReminderAt) {
        reminderNotificationDao.updateLastRemindAt(id, lastReminderAt);
    }

    public List<ReminderNotification> getReminderTimes(int reminderId) {
        return reminderNotificationDao.getReminderTimes(reminderId);
    }

    public ReminderNotification getReminderTime(int id) {
        return reminderNotificationDao.getById(id);
    }
}
