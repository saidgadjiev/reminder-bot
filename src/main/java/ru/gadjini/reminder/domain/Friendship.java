package ru.gadjini.reminder.domain;

public class Friendship {

    private TgUser userOne;

    public TgUser getUserOne() {
        return userOne;
    }

    public void setUserOne(TgUser userOne) {
        this.userOne = userOne;
    }

    public enum Status {

        REQUESTED(0),

        ACCEPTED(1);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public static Status fromCode(int code) {
            for (Status status: values()) {
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
