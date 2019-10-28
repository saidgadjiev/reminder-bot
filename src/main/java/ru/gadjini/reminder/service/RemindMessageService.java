package ru.gadjini.reminder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.RemindMessageDao;
import ru.gadjini.reminder.domain.RemindMessage;

@Service
public class RemindMessageService {

    private RemindMessageDao remindMessageDao;

    @Autowired
    public RemindMessageService(RemindMessageDao remindMessageDao) {
        this.remindMessageDao = remindMessageDao;
    }

    public void create(int reminderId, int messageId) {
        RemindMessage remindMessage = new RemindMessage();

        remindMessage.setReminderId(reminderId);
        remindMessage.setMessageId(messageId);

        remindMessageDao.create(remindMessage);
    }
}
