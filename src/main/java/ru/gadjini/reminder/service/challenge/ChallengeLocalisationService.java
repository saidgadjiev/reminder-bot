package ru.gadjini.reminder.service.challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.time.Time2TextService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.UserUtils;

import java.util.Locale;

@Service
public class ChallengeLocalisationService {

    private LocalisationService localisationService;

    private Time2TextService timeBuilder;

    @Autowired
    public ChallengeLocalisationService(LocalisationService localisationService, Time2TextService timeBuilder) {
        this.localisationService = localisationService;
        this.timeBuilder = timeBuilder;
    }

    public String getChallengeCreator(TgUser creator, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_CREATOR, new Object[]{UserUtils.userLink(creator)}, locale);
    }

    public String getChallengeFinishedAt(DateTime finishedAt, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_FINISHED_AT, new Object[]{timeBuilder.time(finishedAt, locale)}, locale);
    }

    public String getChallengeParticipants(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_PARTICIPANTS, locale);
    }

    public String getChallengeCreated(String challengeName, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_CREATED, new Object[]{challengeName}, locale);
    }

    public String getChallengeDetails(String challengeName, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE, new Object[]{challengeName}, locale);
    }

    public String getChallengeFinished(String challengeName, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_FINISHED, new Object[]{challengeName}, locale);
    }

    public String getChallengeWinner(TgUser winner, int scores, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_WINNER, new Object[]{UserUtils.userLink(winner), scores}, locale);
    }

    public String getChallengesEmpty(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGES_EMPTY, locale);
    }

    public String getChooseParticipantsFooter(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PARTICIPANTS_FOOTER, locale);
    }

    public String getChoseParticipants(String participants, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHOSE_PARTICIPANTS, new Object[]{participants}, locale);
    }

    public String getChallengeInvitation(String creator, String challengeName, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_INVITATION, new Object[]{creator, challengeName}, locale);
    }

    public String getInvitationNotAcceptedYet(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_PARTICIPANT_INVITATION_NOT_ACCEPTED_YET, locale);
    }

    public String getChallengeTotalSeries(int totalSeries, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_CHALLENGE_TOTAL_SERIES, new Object[]{totalSeries}, locale);
    }

    public String getParticipantGaveUp(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_PARTICIPANT_GAVE_UP, locale);
    }
}
