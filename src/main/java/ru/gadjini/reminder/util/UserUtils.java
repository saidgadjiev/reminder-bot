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
        StringBuilder link = new StringBuilder();

        link.append("<a href=\"tg://user?id=").append(user.getUserId()).append("\">").append(user.getName()).append("</a>");

        return link.toString();
    }

    public static String userLink(int id) {
        StringBuilder link = new StringBuilder();

        link.append("<a href=\"tg://user?id=").append(id).append("\">").append(id).append("</a>");

        return link.toString();
    }
}
