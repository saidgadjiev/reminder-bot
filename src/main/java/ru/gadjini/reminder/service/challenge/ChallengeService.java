package ru.gadjini.reminder.service.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ChallengeDao;
import ru.gadjini.reminder.dao.ChallengeParticipantDao;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.model.CreateChallengeRequest;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.challenge.ChallengeReminderService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class ChallengeService {

    private ChallengeDao challengeDao;

    private ChallengeParticipantDao challengeParticipantDao;

    private TimeCreator timeCreator;

    private ReminderRequestService reminderRequestService;

    private ChallengeReminderService challengeReminderService;

    @Autowired
    public ChallengeService(ChallengeDao challengeDao, ChallengeParticipantDao challengeParticipantDao,
                            TimeCreator timeCreator, ReminderRequestService reminderRequestService,
                            ChallengeReminderService challengeReminderService) {
        this.challengeDao = challengeDao;
        this.challengeParticipantDao = challengeParticipantDao;
        this.timeCreator = timeCreator;
        this.reminderRequestService = reminderRequestService;
        this.challengeReminderService = challengeReminderService;
    }

    public void deleteChallenge(int performerId, int challengeId) {
        challengeDao.delete(performerId, challengeId);
    }

    public List<Challenge> getUserChallenges(int userId) {
        return challengeDao.getUserChallenges(userId);
    }

    public Challenge getChallenge(int challengeId) {
        Challenge challenge = challengeDao.getById(challengeId);
        challenge.setChallengeParticipants(challengeParticipantDao.getParticipants(challengeId));

        return challenge;
    }

    @Transactional
    public Challenge createChallenge(User creator, CreateChallengeRequest createChallengeRequest) {
        Challenge challenge = saveChallenge(creator.getId(), createChallengeRequest);

        Set<Integer> participants = createChallengeRequest.participants();
        participants.add(creator.getId());
        List<ChallengeParticipant> challengeParticipants = saveParticipants(creator.getId(), challenge.getId(), participants);
        challenge.setChallengeParticipants(challengeParticipants);

        Reminder reminder = createCreatorAndChallengeReminder(challenge.getId(), creator, createChallengeRequest.reminderRequest());

        challenge.setReminder(reminder);

        return challenge;
    }

    private Challenge saveChallenge(int creatorId, CreateChallengeRequest createChallengeRequest) {
        Challenge challenge = new Challenge();
        challenge.setCreatorId(creatorId);
        challenge.setFinishedAt(getFinishedAt(createChallengeRequest.challengeTime()).withZoneSameInstant(ZoneOffset.UTC));
        challengeDao.save(challenge);

        return challenge;
    }

    private List<ChallengeParticipant> saveParticipants(int creatorId, int challengeId, Collection<Integer> participants) {
        List<ChallengeParticipant> challengeParticipants = new ArrayList<>();

        for (int participant : participants) {
            ChallengeParticipant challengeParticipant = new ChallengeParticipant();
            challengeParticipant.setChallengeId(challengeId);
            challengeParticipant.setUserId(participant);
            if (participant == creatorId) {
                challengeParticipant.setState(ChallengeParticipant.State.ACCEPTED);
                Reminder reminder = new Reminder();
                reminder.setTotalSeries(0);
                challengeParticipant.setReminder(reminder);
            }

            challengeParticipantDao.createParticipant(challengeParticipant);
            challengeParticipants.add(challengeParticipant);
        }

        return challengeParticipants;
    }

    private Reminder createCreatorAndChallengeReminder(int challengeId, User creator, ReminderRequest reminderRequest) {
        reminderRequest.setChallengeId(challengeId);
        Reminder reminder = reminderRequestService.createReminderFromRequest(creator, reminderRequest);
        challengeReminderService.createReminder(reminder);

        return reminder;
    }

    private DateTime getFinishedAt(Time challengeTime) {
        if (challengeTime.isOffsetTime()) {
            return getByOffsetTime(challengeTime.getOffsetTime());
        }

        return challengeTime.getFixedDateTime();
    }

    private DateTime getByOffsetTime(OffsetTime offsetTime) {
        return DateTime.of(JodaTimeUtils.plus(timeCreator.zonedDateTimeNow(offsetTime.getZoneId()), offsetTime.getPeriod()));
    }
}
