package ru.gadjini.reminder.util;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.reminder.domain.TgUser;

public class UserUtils {

    private UserUtils() {}

    public static String name(User user) {
        StringBuilder fio = new StringBuilder();

        fio.append(user.getFirstName());

        if (StringUtils.isNotBlank(user.getLastName())) {
            fio.append(" ").append(user.getLastName());
        }

        return fio.toString();
    }

    public static String userLink(TgUser user) {
        return userLink(user.getUserId(), user.getName());
    }

    public static String userLink(long userId, String name) {
        StringBuilder link = new StringBuilder();

        link.append("<a href=\"tg://user?id=").append(userId).append("\">").append(name).append("</a>");

        return link.toString();
    }

    public static String userLink(long id) {
        StringBuilder link = new StringBuilder();

        link.append("<a href=\"tg://user?id=").append(id).append("\">").append(id).append("</a>");

        return link.toString();
    }
}
