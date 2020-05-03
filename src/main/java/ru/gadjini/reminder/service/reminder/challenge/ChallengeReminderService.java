package ru.gadjini.reminder.service.reminder.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;

@Service
public class ChallengeReminderService {

    private ReminderDao reminderDao;

    @Autowired
    public ChallengeReminderService(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
    }

    public void createReminder(Reminder reminder) {
        reminder.setCreatorId(null);
        reminder.setReceiverId(null);
        reminderDao.create(reminder);
    }

    public Reminder getReminder(int challengeId) {
        return reminderDao.getReminder(ReminderTable.TABLE.as("r").CHALLENGE_ID.eq(challengeId)
                .and(ReminderTable.TABLE.as("r").RECEIVER_ID.isNull()), new ReminderMapping());
    }

    public void deleteReminder(int participantId, int challengeId) {
        reminderDao.delete(
                ReminderTable.TABLE.CREATOR_ID.eq(participantId).and(ReminderTable.TABLE.RECEIVER_ID.eq(participantId))
                        .and(ReminderTable.TABLE.CHALLENGE_ID.eq(challengeId)), null);
    }
}
