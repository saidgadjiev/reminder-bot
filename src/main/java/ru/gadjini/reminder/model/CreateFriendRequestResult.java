package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.Friendship;

public class CreateFriendRequestResult {

    private Friendship friendship;

    private boolean conflict;

    private State state = State.NONE;

    public CreateFriendRequestResult(Friendship friendship, boolean conflict) {
        this.friendship = friendship;
        this.conflict = conflict;
    }

    public Friendship getFriendship() {
        return friendship;
    }

    public boolean isConflict() {
        return conflict;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {

        ALREADY_REQUESTED,

        ALREADY_REQUESTED_TO_ME,

        ALREADY_FRIEND,

        NONE

    }
}
