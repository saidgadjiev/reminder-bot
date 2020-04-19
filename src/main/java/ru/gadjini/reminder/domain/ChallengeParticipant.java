package ru.gadjini.reminder.domain;

public class ChallengeParticipant {

    public static final String TYPE = "challenge_participant";

    public static final String USER_ID = "user_id";

    public static final String CHALLENGE_ID = "challenge_id";

    public static final String INVITATION_ACCEPTED = "invitation_accepted";

    private int userId;

    private int challengeId;

    private Challenge challenge;

    private TgUser user;

    private Reminder reminder;

    private boolean invitationAccepted = false;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public TgUser getUser() {
        return user;
    }

    public void setUser(TgUser user) {
        this.user = user;
    }

    public boolean isInvitationAccepted() {
        return invitationAccepted;
    }

    public void setInvitationAccepted(boolean invitationAccepted) {
        this.invitationAccepted = invitationAccepted;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }
}
