package ru.gadjini.reminder.service.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.dao.ChallengeDao;
import ru.gadjini.reminder.dao.ChallengeParticipantDao;
import ru.gadjini.reminder.dao.ReminderDao;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.mapping.ReminderMapping;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;
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

    private ReminderDao reminderDao;

    private ReminderRequestService reminderRequestService;

    private ChallengeService challengeService;

    @Autowired
    public ChallengeBusinessService(ChallengeParticipantDao participantDao, ChallengeDao challengeDao, ReminderDao reminderDao,
                                    ReminderRequestService reminderRequestService, ChallengeService challengeService) {
        this.participantDao = participantDao;
        this.challengeDao = challengeDao;
        this.reminderDao = reminderDao;
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

    public Winner determineWinner(List<ChallengeParticipant> challengeParticipants) {
        int winnerScore = challengeParticipants.stream().map(ChallengeParticipant::getTotalSeries).mapToInt(Integer::intValue).max().orElse(-1);
        ChallengeParticipant winner = null;
        if (winnerScore != -1) {
            List<ChallengeParticipant> winners = challengeParticipants.stream()
                    .filter(challengeParticipant -> challengeParticipant.getTotalSeries() == winnerScore)
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
