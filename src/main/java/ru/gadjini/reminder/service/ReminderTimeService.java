package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.ReminderTimeDao;
import ru.gadjini.reminder.domain.ReminderTime;

import java.time.LocalDateTime;

@Service
public class ReminderTimeService {

    private ReminderTimeDao reminderTimeDao;

    @Autowired
    public ReminderTimeService(ReminderTimeDao reminderTimeDao) {
        this.reminderTimeDao = reminderTimeDao;
    }

    public void create(ReminderTime reminderTime) {
        reminderTimeDao.create(reminderTime);
    }

    public void deleteReminderTime(int id) {
        reminderTimeDao.delete(id);
    }

    public void updateLastRemindAt(int id, LocalDateTime lastReminderAt) {
        reminderTimeDao.updateLastRemindAt(id, lastReminderAt);
    }
}
