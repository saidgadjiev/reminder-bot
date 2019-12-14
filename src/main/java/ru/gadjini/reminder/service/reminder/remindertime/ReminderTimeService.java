package ru.gadjini.reminder.service.reminder.remindertime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.ReminderTimeDao;
import ru.gadjini.reminder.domain.ReminderTime;

import java.time.LocalDateTime;
import java.util.List;

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

    public void create(List<ReminderTime> reminderTimes) {
        reminderTimeDao.create(reminderTimes);
    }

    public void deleteReminderTimes(int reminderId) {
        reminderTimeDao.deleteByReminderId(reminderId);
    }

    public int deleteReminderTime(int id) {
        return reminderTimeDao.delete(id);
    }

    public void updateLastRemindAt(int id, LocalDateTime lastReminderAt) {
        reminderTimeDao.updateLastRemindAt(id, lastReminderAt);
    }

    public List<ReminderTime> getReminderTimes(int reminderId) {
        return reminderTimeDao.getReminderTimes(reminderId);
    }

    public ReminderTime getReminderTime(int id) {
        return reminderTimeDao.getById(id);
    }
}
