package ru.gadjini.reminder.request;

public enum Arg {

    REMINDER_ID("rid"),
    PREV_HISTORY_NAME("phn"),
    CURR_HISTORY_NAME("chn"),
    RESTORE_KEYBOARD("rk"),
    FRIEND_ID("fid");

    private final String key;

    Arg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
