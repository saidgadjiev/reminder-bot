package ru.gadjini.reminder.service.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.util.UserUtils;

import java.util.List;
import java.util.Locale;

@Service
public class ChallengeMessageBuilder {

    private LocalisationService localisationService;

    private TimeBuilder timeBuilder;

    @Autowired
    public ChallengeMessageBuilder(LocalisationService localisationService, TimeBuilder timeBuilder) {
        this.localisationService = localisationService;
        this.timeBuilder = timeBuilder;
    }

    public String getChallengeCreated(Challenge challenge, Locale locale) {
        StringBuilder message = new StringBuilder();
        message
                .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_CREATED, new Object[]{challenge.getName()}, locale))
                .append("\n");

        message
                .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_FINISHED_AT, new Object[]{timeBuilder.time(challenge.getFinishedAt(), challenge.getCreator().getLocale())}, locale))
                .append("\n");

        message
                .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_CREATOR, new Object[]{UserUtils.userLink(challenge.getId())}, locale))
                .append("\n");

        message
                .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_PARTICIPANTS, locale))
                .append("\n")
                .append(getParticipants(challenge.getChallengeParticipants(), locale));

        return message.toString();
    }

    private String getParticipants(List<ChallengeParticipant> challengeParticipants, Locale locale) {
        StringBuilder participants = new StringBuilder();

        int i = 1;
        for (ChallengeParticipant challengeParticipant: challengeParticipants) {
            if (participants.length() > 0) {
                participants.append("\n");
            }
            participants
                    .append(i++).append(") ").append(UserUtils.userLink(challengeParticipant.getUser())).append("\n")
                    .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_TOTAL_SERIES, new Object[] {challengeParticipant.getTotalSeries()}, locale));
        }

        return participants.toString();
    }
}
