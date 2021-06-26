package ru.gadjini.reminder.request;

public enum Arg {

    REMINDER_ID("a"),
    REMINDER_NOTIFICATION_ID("b"),
    USER_REMINDER_NOTIFICATION_ID("c"),
    PREV_HISTORY_NAME("d"),
    CUSTOM_REMIND_TIME("e"),
    RESTORE_KEYBOARD("f"),
    FRIEND_ID("g"),
    USER_REMINDER_NOTIFICATION_TYPE("h"),
    PLAN_ID("i"),
    SAVED_QUERY_ID("j"),
    POSTPONE_TIME("k"),
    CALLBACK_DELEGATE("l"),
    REASON("m"),
    FILTER("n"),
    CHALLENGE_ID("o"),
    COMMAND_NAME("p"),
    GOAL_ID("r"),
    TAG_ID("s");

    private final String key;

    Arg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
