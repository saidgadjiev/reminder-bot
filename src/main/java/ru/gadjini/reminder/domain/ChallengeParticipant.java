package ru.gadjini.reminder.domain;

import java.util.Objects;

public class ChallengeParticipant {

    public static final String TYPE = "challenge_participant";

    public static final String USER_ID = "user_id";

    public static final String CHALLENGE_ID = "challenge_id";

    public static final String STATE = "state";

    private int userId;

    private int challengeId;

    private Challenge challenge;

    private TgUser user;

    private Reminder reminder;

    private State state;

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
        return Objects.equals(State.ACCEPTED, state);
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Integer getReminderId() {
        return reminder == null ? null : reminder.getId();
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        WAITING(0),
        ACCEPTED(1),
        GAVE_UP(2);

        private final int code;

        State(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static State fromCode(int code) {
            for (State state : values()) {
                if (state.code == code) {
                    return state;
                }
            }

            throw new IllegalArgumentException("Unknown code " + code);
        }
    }
}
