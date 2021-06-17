package ru.gadjini.reminder.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.challenge.ChallengeBusinessService;
import ru.gadjini.reminder.service.challenge.ChallengeMessageBuilder;
import ru.gadjini.reminder.service.challenge.ChallengeService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.util.DateTimeService;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ChallengeJob {

    private DateTimeService timeCreator;

    private ChallengeBusinessService businessService;

    private MessageService messageService;

    private ChallengeMessageBuilder messageBuilder;

    private ChallengeService challengeService;

    @Autowired
    public ChallengeJob(DateTimeService timeCreator, ChallengeBusinessService businessService, MessageService messageService, ChallengeMessageBuilder messageBuilder, ChallengeService challengeService) {
        this.timeCreator = timeCreator;
        this.businessService = businessService;
        this.messageService = messageService;
        this.messageBuilder = messageBuilder;
        this.challengeService = challengeService;
    }

    @PostConstruct
    public void init() {
        finishChallenges();
    }

    //0:30 1:00 1:30 ...
    @Scheduled(cron = "0 */30 * * * *")
    public void finishChallenges() {
        List<Challenge> finishedChallenges = businessService.getFinishedChallenges(timeCreator.localDateTimeNowWithMinutes());
        for (Challenge challenge : finishedChallenges) {
            ChallengeBusinessService.Winner winner = businessService.determineWinner(challenge.getChallengeParticipants());
            sendChallengeFinishedMessages(challenge, winner);
            challengeService.deleteChallenge(challenge.getCreatorId(), challenge.getId());
        }
    }

    private void sendChallengeFinishedMessages(Challenge challenge, ChallengeBusinessService.Winner winner) {
        for (ChallengeParticipant participant : challenge.getChallengeParticipants()) {
            String message = messageBuilder.getChallengeFinished(participant.getUserId(), challenge, winner, participant.getUser().getLocale());
            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(participant.getUserId())
                            .text(message)
            );
        }
    }
}
