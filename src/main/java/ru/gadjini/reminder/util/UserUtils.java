package ru.gadjini.reminder.util;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.reminder.domain.TgUser;

public class UserUtils {

    private UserUtils() {}

    public static String fio(TgUser user) {
        StringBuilder fio = new StringBuilder();

        fio.append(user.getFirstName());

        if (StringUtils.isNotBlank(user.getLastName())) {
            fio.append(" ").append(user.getLastName());
        }

        return fio.toString();
    }

    public static String userLink(TgUser user) {
        StringBuilder link = new StringBuilder();

        link.append("<a href=\"tg://user?id=").append(user.getUserId()).append("\">").append(fio(user)).append("</a>");

        return link.toString();
    }
}
