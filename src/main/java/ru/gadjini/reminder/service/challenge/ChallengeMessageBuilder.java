package ru.gadjini.reminder.service.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.util.UserUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ChallengeMessageBuilder {

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private LocalisationService localisationService;

    private TimeBuilder timeBuilder;

    @Autowired
    public ChallengeMessageBuilder(FriendshipMessageBuilder friendshipMessageBuilder, LocalisationService localisationService, TimeBuilder timeBuilder) {
        this.friendshipMessageBuilder = friendshipMessageBuilder;
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
                .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_CREATOR, new Object[]{UserUtils.userLink(challenge.getCreator())}, locale))
                .append("\n");

        message
                .append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_PARTICIPANTS, locale))
                .append("\n")
                .append(getParticipants(challenge.getChallengeParticipants(), locale));

        return message.toString();
    }

    public String getFriendsListWithChoseParticipantsInfo(List<TgUser> friends, Set<Integer> participants, Locale locale) {
        String friendsList = friendshipMessageBuilder.getFriendsList(friends, MessagesProperties.MESSAGE_FRIENDS_EMPTY,
                MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_HEADER, null, locale);
        if (participants.isEmpty()) {
            return friendsList +
                    "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_FOOTER, locale);
        }
        StringBuilder selectedParticipants = new StringBuilder();

        for (TgUser userData : friends) {
            if (selectedParticipants.length() > 0) {
                selectedParticipants.append(", ");
            }
            if (participants.contains(userData.getUserId())) {
                selectedParticipants.append(UserUtils.userLink(userData.getUserId(), userData.getName()));
            }
        }

        return friendsList
                + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CHOSE_PARTICIPANTS, new Object[]{selectedParticipants.toString()}, locale)
                + "\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_FOOTER, locale);
    }

    public String getChallengeInvitation(Challenge challenge, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_INVITATION, new Object[]{UserUtils.userLink(challenge.getCreator()), challenge.getName()}, locale);
    }

    private String getParticipants(List<ChallengeParticipant> challengeParticipants, Locale locale) {
        StringBuilder participants = new StringBuilder();

        int i = 1;
        for (ChallengeParticipant challengeParticipant : challengeParticipants) {
            if (participants.length() > 0) {
                participants.append("\n");
            }
            participants.append(i++).append(") ").append(UserUtils.userLink(challengeParticipant.getUser()));
            if (challengeParticipant.isInvitationAccepted()) {
                participants.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_TOTAL_SERIES, new Object[]{challengeParticipant.getTotalSeries()}, locale));
            } else {
                participants.append(" (").append(localisationService.getMessage(MessagesProperties.MESSAGE_PARTICIPANT_INVITATION_NOT_ACCEPTED_YET, locale)).append(")");
            }
        }

        return participants.toString();
    }
}
