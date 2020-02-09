package ru.gadjini.reminder.bot.command.callback.state;

import ru.gadjini.reminder.domain.TgUser;

public class UserData {

    private String name;

    private int userId;

    private String zoneId;

    private String languageCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public static TgUser to(UserData userData) {
        TgUser user = new TgUser();

        user.setChatId(userData.getUserId());
        user.setUserId(userData.getUserId());
        user.setName(userData.getName());
        user.setZoneId(userData.getZoneId());
        user.setLanguageCode(userData.getLanguageCode());

        return user;
    }

    public static UserData from(TgUser user) {
        UserData userData = new UserData();

        userData.setName(user.getName());
        userData.setUserId(user.getUserId());
        userData.setZoneId(user.getZoneId());
        userData.setLanguageCode(user.getLanguageCode());

        return userData;
    }
}
