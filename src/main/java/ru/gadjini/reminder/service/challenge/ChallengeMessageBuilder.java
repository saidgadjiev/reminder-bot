package ru.gadjini.reminder.service.challenge;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.message.MessageBuilder;
import ru.gadjini.reminder.util.UserUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ChallengeMessageBuilder {

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private LocalisationService localisationService;

    private FriendshipService friendshipService;

    private MessageBuilder messageBuilder;

    @Autowired
    public ChallengeMessageBuilder(FriendshipMessageBuilder friendshipMessageBuilder, LocalisationService localisationService,
                                   FriendshipService friendshipService, MessageBuilder messageBuilder) {
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
        this.messageBuilder = messageBuilder;
    }

    public String getChallengeFinished(int requesterId, Challenge challenge, ChallengeBusinessService.Winner winner, Locale locale) {
        StringBuilder message = new StringBuilder();
        message
                .append(messageBuilder.getChallengeFinished(challenge.getName(), locale)).append("\n");

        if (winner.getWinnerState() == ChallengeBusinessService.WinnerState.WINNER) {
            message.append(messageBuilder.getChallengeWinner(winner.getWinner().getUser(), winner.getWinner().getTotalSeries(), locale)).append("\n");
        }

        message.append(messageBuilder.getChallengeCreator(challenge.getCreator(), locale)).append("\n")
                .append(messageBuilder.getChallengeParticipants(locale)).append("\n")
                .append(getParticipants(requesterId, challenge.getChallengeParticipants(), locale));

        return message.toString();
    }

    public String getUserChallenges(List<Challenge> challenges, Locale locale) {
        StringBuilder message = new StringBuilder();
        int i = 1;
        for (Challenge challenge : challenges) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message
                    .append(i++).append(") ").append(challenge.getName()).append("\n")
                    .append(messageBuilder.getChallengeCreator(challenge.getCreator(), locale));
        }

        return message.toString();
    }

    public String getChallengeDetails(int requesterId, Challenge challenge, Locale locale) {
        StringBuilder message = new StringBuilder();
        message
                .append(messageBuilder.getChallengeDetails(challenge.getName(), locale)).append("\n")
                .append(messageBuilder.getChallengeFinishedAt(challenge.getFinishedAt(), locale)).append("\n")
                .append(messageBuilder.getChallengeCreator(challenge.getCreator(), locale)).append("\n")
                .append(messageBuilder.getChallengeParticipants(locale)).append("\n")
                .append(getParticipants(requesterId, challenge.getChallengeParticipants(), locale));

        return message.toString();
    }

    public String getChallengeCreatedDetails(int requesterId, Challenge challenge, Locale locale) {
        StringBuilder message = new StringBuilder();
        message
                .append(messageBuilder.getChallengeCreated(challenge.getName(), locale)).append("\n")
                .append(messageBuilder.getChallengeFinishedAt(challenge.getFinishedAt(), locale)).append("\n")
                .append(messageBuilder.getChallengeCreator(challenge.getCreator(), locale)).append("\n")
                .append(messageBuilder.getChallengeParticipants(locale)).append("\n")
                .append(getParticipants(requesterId, challenge.getChallengeParticipants(), locale));

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

    public String getChallengeInvitation(Challenge challenge, int participantUserId, Locale locale) {
        String friendName = friendshipService.getFriendName(participantUserId, challenge.getCreatorId());

        return localisationService.getMessage(
                MessagesProperties.MESSAGE_CHALLENGE_INVITATION,
                new Object[]{UserUtils.userLink(challenge.getCreatorId(), friendName), challenge.getName()},
                locale
        );
    }

    private String getParticipants(int creatorId, List<ChallengeParticipant> challengeParticipants, Locale locale) {
        StringBuilder participants = new StringBuilder();

        int i = 1;
        for (ChallengeParticipant challengeParticipant : challengeParticipants) {
            if (participants.length() > 0) {
                participants.append("\n");
            }
            String friendName = friendshipService.getFriendName(creatorId, challengeParticipant.getUserId());
            participants.append(i++).append(") ").append(UserUtils.userLink(challengeParticipant.getUserId(), StringUtils.isBlank(friendName) ? challengeParticipant.getUser().getName() : friendName));
            if (challengeParticipant.isInvitationAccepted()) {
                participants.append("\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_TOTAL_SERIES, new Object[]{challengeParticipant.getTotalSeries()}, locale));
            } else {
                participants.append(" (").append(localisationService.getMessage(MessagesProperties.MESSAGE_PARTICIPANT_INVITATION_NOT_ACCEPTED_YET, locale)).append(")");
            }
        }

        return participants.toString();
    }
}
