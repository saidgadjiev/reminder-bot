package ru.gadjini.reminder.domain;

public class SavedQuery {

    public static final String ID = "id";

    public static final String QUERY = "query";

    public static final String USER_ID = "user_id";

    private int id;

    private String query;

    private long userId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
