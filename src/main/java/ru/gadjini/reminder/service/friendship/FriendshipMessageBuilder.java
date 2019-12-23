package ru.gadjini.reminder.service.friendship;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTimeFormats;
import ru.gadjini.reminder.util.UserUtils;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class FriendshipMessageBuilder {

    private LocalisationService localisationService;

    public FriendshipMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String getFriendDetails(TgUser friend) {
        StringBuilder message = new StringBuilder();
        message.append(UserUtils.userLink(friend)).append("\n\n");
        message.append(localisationService.getMessage(MessagesProperties.TIMEZONE, new Object[] {
                friend.getZoneId(),
                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(ZonedDateTime.now(friend.getZone()))
        }));

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
            message.append("\n\n").append(localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_FRIEND_REQUEST_CANCEL));
        }

        return message.toString();
    }
}
