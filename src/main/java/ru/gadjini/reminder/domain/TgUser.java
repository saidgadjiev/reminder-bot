package ru.gadjini.reminder.domain;

import org.apache.commons.lang3.StringUtils;

public class TgUser {

    public static final String TYPE = "tg_user";

    public static final String USERNAME_START = "@";

    public static final String ID = "id";

    public static final String CHAT_ID = "chat_id";

    public static final String USERNAME = "username";

    public static final String FIRST_NAME = "first_name";

    public static final String LAST_NAME = "last_name";

    private int id;

    private long chatId;

    private String username;

    private String firstName;

    private String lastName;

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

    public String getFio() {
        StringBuilder fio = new StringBuilder();

        fio.append(firstName);

        if (StringUtils.isNotBlank(lastName)) {
            fio.append(" ").append(lastName);
        }

        return fio.toString();
    }
}
