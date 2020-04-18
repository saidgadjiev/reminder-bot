package ru.gadjini.reminder.service.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.dao.ChallengeDao;
import ru.gadjini.reminder.dao.ChallengeParticipantDao;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.model.CreateChallengeRequest;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    private ChallengeDao challengeDao;

    private ChallengeParticipantDao challengeParticipantDao;

    private LocalisationService localisationService;

    private TgUserService userService;

    private TimeCreator timeCreator;

    private ReminderRequestService reminderRequestService;

    @Autowired
    public ChallengeService(ChallengeDao challengeDao, ChallengeParticipantDao challengeParticipantDao,
                            LocalisationService localisationService, TgUserService userService,
                            TimeCreator timeCreator, ReminderRequestService reminderRequestService) {
        this.challengeDao = challengeDao;
        this.challengeParticipantDao = challengeParticipantDao;
        this.localisationService = localisationService;
        this.userService = userService;
        this.timeCreator = timeCreator;
        this.reminderRequestService = reminderRequestService;
    }

    public void deleteChallenge(int challengeId) {
        challengeDao.delete(challengeId);
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
        validateTime(creator.getId(), createChallengeRequest.challengeTime());
        Challenge challenge = saveChallenge(creator.getId(), createChallengeRequest);

        Set<Integer> participants = createChallengeRequest.participants();
        participants.add(creator.getId());
        List<ChallengeParticipant> challengeParticipants = saveParticipants(creator.getId(), challenge.getId(), participants);
        challenge.setChallengeParticipants(challengeParticipants);

        createCreatorReminder(challenge.getId(), creator, createChallengeRequest.reminderRequest());

        return challenge;
    }

    private Challenge saveChallenge(int creatorId, CreateChallengeRequest createChallengeRequest) {
        Challenge challenge = new Challenge();
        challenge.setCreatorId(creatorId);
        challenge.setName(createChallengeRequest.reminderRequest().getText());
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
            challengeParticipant.setInvitationAccepted(participant == creatorId);

            challengeParticipantDao.createParticipant(challengeParticipant);
            challengeParticipants.add(challengeParticipant);
        }

        return challengeParticipants;
    }

    private void createCreatorReminder(int challengeId, User creator, ReminderRequest reminderRequest) {
        reminderRequest.setChallengeId(challengeId);
        reminderRequestService.createReminderFromRequest(creator, reminderRequest);
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

    private void validateTime(int creatorId, Time time) {
        if (time.isRepeatTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_CHALLENGE_TIME, userService.getLocale(creatorId)));
        }
        if (time.isOffsetTime() && !time.getOffsetTime().getType().equals(OffsetTime.Type.FOR)) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_CHALLENGE_TIME, userService.getLocale(creatorId)));
        }
    }
}
