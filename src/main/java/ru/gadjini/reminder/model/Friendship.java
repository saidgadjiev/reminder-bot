package ru.gadjini.reminder.model;

import ru.gadjini.reminder.domain.TgUser;

public class Friendship {

    private TgUser userOne;

    public TgUser getUserOne() {
        return userOne;
    }

    public void setUserOne(TgUser userOne) {
        this.userOne = userOne;
    }

    public enum Status {

        REQUESTED,

        ACCEPTED
    }
}
