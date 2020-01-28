package ru.gadjini.reminder.bot.command.callback.postpone;

import ru.gadjini.reminder.domain.TgUser;

public class UserData {

    private String name;

    private int userId;

    private String zoneId;

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

    public static TgUser to(UserData userData) {
        TgUser user = new TgUser();

        user.setChatId(userData.getUserId());
        user.setUserId(userData.getUserId());
        user.setName(userData.getName());
        user.setZoneId(userData.getZoneId());

        return user;
    }

    public static UserData from(TgUser user) {
        UserData userData = new UserData();

        userData.setName(user.getName());
        userData.setUserId(user.getUserId());
        userData.setZoneId(user.getZoneId());

        return userData;
    }
}
