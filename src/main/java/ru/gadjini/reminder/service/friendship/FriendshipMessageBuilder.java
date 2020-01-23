package ru.gadjini.reminder.service.friendship;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTimeFormats;
import ru.gadjini.reminder.util.TimeUtils;
import ru.gadjini.reminder.util.UserUtils;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class FriendshipMessageBuilder {

    private LocalisationService localisationService;

    public FriendshipMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String getFriendDetails(TgUser friend) {
        return getFriendDetails(friend, null);
    }

    public String getFriendDetailsWithFooterCode(TgUser friend, String footerCode) {
        return getFriendDetails(friend, localisationService.getMessage(footerCode));
    }

    public String getFriendDetails(TgUser friend, String footer) {
        StringBuilder message = new StringBuilder();

        message.append(UserUtils.userLink(friend)).append("\n\n");
        message.append(localisationService.getMessage(MessagesProperties.TIMEZONE, new Object[] {
                friend.getZone().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(TimeUtils.zonedDateTimeNow(friend.getZone()))
        }));

        if (StringUtils.isNotBlank(footer)) {
            message.append("\n\n").append(footer);
        }

        return message.toString();
    }


    public String getFriendsList(List<TgUser> items, String emptyCode, String footer) {
        if (items.isEmpty()) {
            return localisationService.getMessage(emptyCode);
        }
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (TgUser friend : items) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(UserUtils.userLink(friend));
        }
        if (StringUtils.isNotBlank(footer)) {
            message.append("\n\n").append(localisationService.getMessage(footer));
        }

        return message.toString();
    }
}
