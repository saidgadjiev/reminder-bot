package ru.gadjini.reminder.domain;

public class ChallengeParticipant {

    public static final String TYPE = "challenge_participant";

    public static final String USER_ID = "user_id";

    public static final String CHALLENGE_ID = "challenge_id";

    private int userId;

    private int challengeId;

    private TgUser user;

    private int totalSeries = 0;

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

    public int getTotalSeries() {
        return totalSeries;
    }

    public void setTotalSeries(int totalSeries) {
        this.totalSeries = totalSeries;
    }

    public boolean isInvitationAccepted() {
        return invitationAccepted;
    }

    public void setInvitationAccepted(boolean invitationAccepted) {
        this.invitationAccepted = invitationAccepted;
    }
}
