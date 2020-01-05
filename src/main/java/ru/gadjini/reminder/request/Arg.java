package ru.gadjini.reminder.request;

public enum Arg {

    REMINDER_ID("a"),
    REMINDER_NOTIFICATION_ID("b"),
    USER_REMINDER_NOTIFICATION_ID("c"),
    PREV_HISTORY_NAME("d"),
    CURR_HISTORY_NAME("e"),
    RESTORE_KEYBOARD("f"),
    FRIEND_ID("g"),
    USER_REMINDER_NOTIFICATION_TYPE("h"),
    PLAN_ID("i");

    private final String key;

    Arg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
