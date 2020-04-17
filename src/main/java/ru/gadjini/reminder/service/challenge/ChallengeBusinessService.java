package ru.gadjini.reminder.service.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ChallengeParticipantDao;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;

import java.time.ZoneOffset;

@Service
public class ChallengeBusinessService {

    private ChallengeParticipantDao participantDao;

    private ReminderDao reminderDao;

    private ReminderRequestService reminderRequestService;

    private ChallengeService challengeService;

    @Autowired
    public ChallengeBusinessService(ChallengeParticipantDao participantDao, ReminderDao reminderDao,
                                    ReminderRequestService reminderRequestService, ChallengeService challengeService) {
        this.participantDao = participantDao;
        this.reminderDao = reminderDao;
        this.reminderRequestService = reminderRequestService;
        this.challengeService = challengeService;
    }

    @Transactional
    public Challenge acceptChallenge(User participant, int challengeId) {
        participantDao.updateInvitationAccepted(participant.getId(), challengeId, true);
        Reminder challengeReminder = reminderDao.getReminder(ReminderTable.TABLE.as("r").CHALLENGE_ID.eq(challengeId),
                new ReminderMapping()
        );
        ReminderRequest reminderRequest = createReminderRequest(challengeReminder, participant.getId());
        reminderRequestService.createReminderFromRequest(participant, reminderRequest);

        return challengeService.getChallenge(challengeId);
    }

    public void rejectChallenge(int participantId, int challengeId) {
        participantDao.delete(participantId, challengeId);
    }

    private ReminderRequest createReminderRequest(Reminder reminder, int participant) {
        ReminderRequest reminderRequest = new ReminderRequest();
        reminderRequest.setCreatorId(participant);
        reminderRequest.setReceiverId(participant);
        reminderRequest.setText(reminder.getText());
        reminderRequest.setChallengeId(reminder.getChallengeId());

        Time reminderTime = new Time(ZoneOffset.UTC);
        reminderTime.setRepeatTimes(reminder.getRepeatRemindAts());
        reminderRequest.setTime(reminderTime);

        return reminderRequest;
    }
}
