package ru.gadjini.reminder.domain;

public class Tag {

    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String TAG = "tag";

    private int id;

    private long userId;

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
