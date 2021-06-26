package ru.gadjini.reminder.service.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.ReminderTagDao;
import ru.gadjini.reminder.domain.Tag;

import java.util.List;

@Service
public class ReminderTagService {

    public static final int NO_TAG_ID = -1;

    private ReminderTagDao reminderTagDao;

    @Autowired
    public ReminderTagService(ReminderTagDao reminderTagDao) {
        this.reminderTagDao = reminderTagDao;
    }

    public List<Tag> getTags(long userId) {
        return reminderTagDao.tags(userId);
    }

    public List<String> getReminderTags(int reminderId) {
        return reminderTagDao.reminderTags(reminderId);
    }

    public void tag(int reminderId, int tagId) {
        reminderTagDao.tag(reminderId, tagId);
    }
}
