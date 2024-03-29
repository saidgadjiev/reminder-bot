package ru.gadjini.reminder.service.challenge;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Challenge;
import ru.gadjini.reminder.domain.ChallengeParticipant;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.friendship.FriendshipMessageBuilder;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.reminder.time.Time2TextService;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ChallengeMessageBuilder {

    private FriendshipMessageBuilder friendshipMessageBuilder;

    private FriendshipService friendshipService;

    private ChallengeLocalisationService messageBuilder;

    private Time2TextService timeBuilder;

    private TgUserService userService;

    @Autowired
    public ChallengeMessageBuilder(FriendshipMessageBuilder friendshipMessageBuilder,
                                   FriendshipService friendshipService, ChallengeLocalisationService messageBuilder,
                                   Time2TextService timeBuilder, TgUserService userService) {
        this.friendshipMessageBuilder = friendshipMessageBuilder;
        this.friendshipService = friendshipService;
        this.messageBuilder = messageBuilder;
        this.timeBuilder = timeBuilder;
        this.userService = userService;
    }

    public String getChallengeFinished(long requesterId, Challenge challenge, ChallengeBusinessService.Winner winner, Locale locale) {
        StringBuilder message = new StringBuilder();
        message
                .append(messageBuilder.getChallengeFinished(getChallengeName(challenge.getReminder(), locale), locale)).append("\n");

        if (winner.getWinnerState() == ChallengeBusinessService.WinnerState.WINNER) {
            message.append(messageBuilder.getChallengeWinner(winner.getWinner().getUser(), winner.getWinner().getReminder().getTotalSeries(), locale)).append("\n");
        }

        message.append(messageBuilder.getChallengeCreator(challenge.getCreator(), locale)).append("\n")
                .append(messageBuilder.getChallengeParticipants(locale)).append("\n")
                .append(getParticipants(requesterId, challenge.getChallengeParticipants(), locale));

        return message.toString();
    }

    public String getUserChallenges(List<Challenge> challenges, Locale locale) {
        if (challenges.isEmpty()) {
            return messageBuilder.getChallengesEmpty(locale);
        }
        StringBuilder message = new StringBuilder();
        int i = 1;
        for (Challenge challenge : challenges) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message
                    .append(i++).append(") ").append(getChallengeName(challenge.getReminder(), locale)).append("\n")
                    .append(messageBuilder.getChallengeCreator(challenge.getCreator(), locale));
        }

        return message.toString();
    }

    public String getChallengeDetails(long requesterId, Challenge challenge, Locale locale) {
        ZoneId zoneId = userService.getTimeZone(requesterId);

        return messageBuilder.getChallengeDetails(getChallengeName(challenge.getReminder(), locale), locale) + "\n" +
                messageBuilder.getChallengeFinishedAt(challenge.getFinishedAt().withZoneSameInstant(zoneId), locale) + "\n" +
                messageBuilder.getChallengeCreator(challenge.getCreator(), locale) + "\n" +
                messageBuilder.getChallengeParticipants(locale) + "\n" +
                getParticipants(requesterId, challenge.getChallengeParticipants(), locale);
    }

    public String getChallengeCreatedDetails(long requesterId, Challenge challenge, Locale locale) {
        ZoneId zoneId = userService.getTimeZone(requesterId);

        return messageBuilder.getChallengeCreated(getChallengeName(challenge.getReminder(), locale), locale) + "\n" +
                messageBuilder.getChallengeFinishedAt(challenge.getFinishedAt().withZoneSameInstant(zoneId), locale) + "\n" +
                messageBuilder.getChallengeCreator(challenge.getCreator(), locale) + "\n" +
                messageBuilder.getChallengeParticipants(locale) + "\n" +
                getParticipants(requesterId, challenge.getChallengeParticipants(), locale);
    }

    public String getFriendsListWithChoseParticipantsInfo(List<TgUser> friends, Set<Long> participants, Locale locale) {
        String friendsList = friendshipMessageBuilder.getFriendsList(friends, MessagesProperties.MESSAGE_FRIENDS_EMPTY,
                MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_HEADER, null, locale);
        if (participants.isEmpty()) {
            return friendsList + "\n\n" + messageBuilder.getChooseParticipantsFooter(locale);
        }
        StringBuilder selectedParticipants = new StringBuilder();

        for (TgUser userData : friends) {
            if (participants.contains(userData.getUserId())) {
                if (selectedParticipants.length() > 0) {
                    selectedParticipants.append(", ");
                }
                selectedParticipants.append(UserUtils.userLink(userData.getUserId(), userData.getName()));
            }
        }

        return friendsList
                + "\n\n" + messageBuilder.getChoseParticipants(selectedParticipants.toString(), locale)
                + "\n" + messageBuilder.getChooseParticipantsFooter(locale);
    }

    public String getChallengeInvitation(Challenge challenge, long participantUserId, Locale locale) {
        String friendName = friendshipService.getFriendName(participantUserId, challenge.getCreatorId());

        return messageBuilder.getChallengeInvitation(
                UserUtils.userLink(challenge.getCreatorId(), friendName), getChallengeName(challenge.getReminder(), locale),
                locale
        );
    }

    private String getParticipants(long requesterId, List<ChallengeParticipant> challengeParticipants, Locale locale) {
        StringBuilder participants = new StringBuilder();

        int i = 1;
        for (ChallengeParticipant challengeParticipant : challengeParticipants) {
            if (participants.length() > 0) {
                participants.append("\n");
            }
            if (requesterId == challengeParticipant.getUserId()) {
                participants.append(i++).append(") ").append(UserUtils.userLink(challengeParticipant.getUserId(), challengeParticipant.getUser().getName()));
            } else {
                String friendName = friendshipService.getFriendName(requesterId, challengeParticipant.getUserId());
                participants.append(i++).append(") ").append(UserUtils.userLink(challengeParticipant.getUserId(), StringUtils.isBlank(friendName) ? challengeParticipant.getUser().getName() : friendName));
            }
            participants.append(participantStateToString(challengeParticipant, locale));
        }

        return participants.toString();
    }

    private String participantStateToString(ChallengeParticipant challengeParticipant, Locale locale) {
        StringBuilder state = new StringBuilder();
        switch (challengeParticipant.getState()) {
            case WAITING:
                state.append(" (").append(messageBuilder.getInvitationNotAcceptedYet(locale)).append(")");
                break;
            case ACCEPTED:
                state.append("\n").append(messageBuilder.getChallengeTotalSeries(challengeParticipant.getReminder().getTotalSeries(), locale));
                break;
            case GAVE_UP:
                state.append(" (").append(messageBuilder.getParticipantGaveUp(locale)).append(")");
                break;
        }

        return state.toString();
    }

    private String getChallengeName(Reminder reminder, Locale locale) {
        StringBuilder message = new StringBuilder();
        message.append(reminder.getText());
        String time = timeBuilder.time(reminder.getRepeatRemindAts(), locale);
        if (StringUtils.isNotBlank(time)) {
            message.append(" ").append(time);
        }

        return message.toString();
    }
}
