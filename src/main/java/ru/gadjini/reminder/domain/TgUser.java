package ru.gadjini.reminder.domain;

import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZoneId;

public class TgUser {

    public static final String TYPE = "tg_user";

    public static final String USERNAME_START = "@";

    public static final String CHAT_ID = "chat_id";

    public static final String USERNAME = "username";

    public static final String NAME = "name";

    public static final String USER_ID = "user_id";

    public static final String ZONE_ID = "zone_id";

    public static final String ID = "id";

    private long chatId;

    private String username;

    private String name;

    private int userId;

    private String zoneId;

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

    public ZoneId getZone() {
        return ZoneId.of(zoneId);
    }

    public void setZone(ZoneId zone) {
        this.zoneId = zone.getId();
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public void setFrom(User user) {
        this.userId = user.getId();
        this.name = UserUtils.name(user);
    }

    public static TgUser from(User user) {
        TgUser tgUser = new TgUser();

        tgUser.setName(UserUtils.name(user));
        tgUser.setUserId(user.getId());

        return tgUser;
    }
}
