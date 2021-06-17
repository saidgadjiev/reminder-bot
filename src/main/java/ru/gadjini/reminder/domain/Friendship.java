package ru.gadjini.reminder.domain;

public class Friendship {

    public static final String TYPE = "friendship";

    public static final String USER_ONE_ID = "user_one_id";

    public static final String USER_TWO_ID = "user_two_id";

    public static final String USER_ONE_NAME = "user_one_name";

    public static final String USER_TWO_NAME = "user_two_name";

    public static final String STATUS = "status";

    private Status status;

    private long userOneId;

    private long userTwoId;

    private String userOneName;

    private String userTwoName;

    private TgUser userOne;

    private TgUser userTwo;

    public String getUserOneName() {
        return userOneName;
    }

    public void setUserOneName(String userOneName) {
        this.userOneName = userOneName;
    }

    public String getUserTwoName() {
        return userTwoName;
    }

    public void setUserTwoName(String userTwoName) {
        this.userTwoName = userTwoName;
    }

    public long getUserOneId() {
        return userOneId;
    }

    public void setUserOneId(long userOneId) {
        this.userOneId = userOneId;
    }

    public long getUserTwoId() {
        return userTwoId;
    }

    public void setUserTwoId(long userTwoId) {
        this.userTwoId = userTwoId;
    }

    public TgUser getUserOne() {
        return userOne;
    }

    public void setUserOne(TgUser userOne) {
        this.userOne = userOne;
    }

    public TgUser getUserTwo() {
        return userTwo;
    }

    public void setUserTwo(TgUser userTwo) {
        this.userTwo = userTwo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TgUser getFriend(long userId) {
        TgUser friend = new TgUser();

        if (getUserOneId() == userId) {
            friend.setUserId(getUserTwoId());
            friend.setName(getUserTwoName());
        } else if (getUserTwoId() == userId) {
            friend.setUserId(getUserOneId());
            friend.setName(getUserOneName());
        }

        return friend;
    }

    public TgUser getUser(long userId) {
        TgUser friend = new TgUser();

        if (getUserOneId() == userId) {
            friend.setUserId(getUserOneId());
            friend.setName(getUserOneName());
        } else if (getUserTwoId() == userId) {
            friend.setUserId(getUserTwoId());
            friend.setName(getUserTwoName());
        }

        return friend;
    }

    public enum Status {

        REQUESTED(0),

        ACCEPTED(1),

        REJECTED(2);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public static Status fromCode(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }

            throw new IllegalArgumentException();
        }

        public int getCode() {
            return code;
        }
    }
}
