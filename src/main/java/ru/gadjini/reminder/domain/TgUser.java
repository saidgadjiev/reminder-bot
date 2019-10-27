package ru.gadjini.reminder.domain;

import org.telegram.telegrambots.meta.api.objects.User;

public class TgUser {

    public static final String TYPE = "tg_user";

    public static final String USERNAME_START = "@";

    public static final String ID = "id";

    public static final String CHAT_ID = "chat_id";

    public static final String USERNAME = "username";

    public static final String FIRST_NAME = "first_name";

    public static final String LAST_NAME = "last_name";

    public static final String USER_ID = "user_id";

    public static final String ZONE_ID = "zone_id";

    private int id;

    private long chatId;

    private String username;

    private String firstName;

    private String lastName;

    private int userId;

    private String zoneId;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public void setFrom(User user) {
        this.userId = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    public static TgUser from(User user) {
        TgUser tgUser = new TgUser();

        tgUser.setFirstName(user.getFirstName());
        tgUser.setLastName(user.getLastName());
        tgUser.setUserId(user.getId());

        return tgUser;
    }
}
