package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.ReminderTimeDao;
import ru.gadjini.reminder.domain.ReminderTime;

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
}
