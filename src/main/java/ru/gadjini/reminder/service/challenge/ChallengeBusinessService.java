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
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
import ru.gadjini.reminder.service.reminder.challenge.ChallengeReminderService;
import ru.gadjini.reminder.service.reminder.ReminderRequestService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChallengeBusinessService {

    private ChallengeParticipantDao participantDao;

    private ChallengeDao challengeDao;

    private ChallengeReminderService challengeReminderService;

    private ReminderRequestService reminderRequestService;

    private ChallengeService challengeService;

    @Autowired
    public ChallengeBusinessService(ChallengeParticipantDao participantDao, ChallengeDao challengeDao,
                                    ChallengeReminderService challengeReminderService,
                                    ReminderRequestService reminderRequestService, ChallengeService challengeService) {
        this.participantDao = participantDao;
        this.challengeDao = challengeDao;
        this.challengeReminderService = challengeReminderService;
        this.reminderRequestService = reminderRequestService;
        this.challengeService = challengeService;
    }

    public List<Challenge> getFinishedChallenges(LocalDateTime localDateTime) {
        List<Challenge> challenges = challengeDao.getChallenges(localDateTime);
        Map<Integer, List<ChallengeParticipant>> challengeParticipants = participantDao.getParticipants(challenges.stream().map(Challenge::getId).collect(Collectors.toList()));

        for (Challenge challenge : challenges) {
            challenge.setChallengeParticipants(challengeParticipants.get(challenge.getId()));
        }

        return challenges;
    }

    @Transactional
    public void exit(User participant, int challengeId) {
        participantDao.delete(participant.getId(), challengeId);
        challengeReminderService.deleteReminder(participant.getId(), challengeId);
    }

    @Transactional
    public Challenge giveUp(User participant, int challengeId) {
        participantDao.updateState(participant.getId(), challengeId, ChallengeParticipant.State.GAVE_UP);
        challengeReminderService.deleteReminder(participant.getId(), challengeId);

        return challengeService.getChallenge(challengeId);
    }

    @Transactional
    public Challenge acceptChallenge(User participant, int challengeId) {
        participantDao.updateState(participant.getId(), challengeId, ChallengeParticipant.State.ACCEPTED);
        Reminder challengeReminder = challengeReminderService.getReminder(challengeId);
        ReminderRequest reminderRequest = createReminderRequest(challengeReminder, participant.getId());
        reminderRequestService.createReminderFromRequest(participant, reminderRequest);

        return challengeService.getChallenge(challengeId);
    }

    public void rejectChallenge(long participantId, int challengeId) {
        participantDao.delete(participantId, challengeId);
    }

    private ReminderRequest createReminderRequest(Reminder reminder, long participant) {
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

    public Winner determineWinner(List<ChallengeParticipant> challengeParticipants) {
        challengeParticipants = challengeParticipants.stream()
                .filter(challengeParticipant -> challengeParticipant.getState() == ChallengeParticipant.State.ACCEPTED)
                .collect(Collectors.toList());

        int winnerScore = challengeParticipants.stream()
                .map(participant -> participant.getReminder().getTotalSeries())
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);

        ChallengeParticipant winner = null;
        if (winnerScore != -1) {
            List<ChallengeParticipant> winners = challengeParticipants.stream()
                    .filter(challengeParticipant -> challengeParticipant.getReminder().getTotalSeries() == winnerScore)
                    .collect(Collectors.toList());

            winner = winners.size() > 1 ? null : winners.get(0);
        }

        return new Winner(winnerScore != -1 ? WinnerState.WINNER : WinnerState.NO_WINNER, winner);
    }

    public static class Winner {

        private WinnerState winnerState;

        private ChallengeParticipant winner;

        private Winner(WinnerState winnerState, ChallengeParticipant winner) {
            this.winnerState = winnerState;
            this.winner = winner;
        }

        public WinnerState getWinnerState() {
            return winnerState;
        }

        public ChallengeParticipant getWinner() {
            return winner;
        }
    }

    public enum WinnerState {
        WINNER,
        NO_WINNER
    }
}
