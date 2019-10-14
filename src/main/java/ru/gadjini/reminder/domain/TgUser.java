package ru.gadjini.reminder.domain;

public class TgUser {

    public static final String TYPE = "tg_user";

    public static final String USERNAME_START = "@";

    public static final String ID = "id";

    public static final String CHAT_ID = "chat_id";

    public static final String USERNAME = "username";

    private int id;

    private long chatId;

    private String username;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
