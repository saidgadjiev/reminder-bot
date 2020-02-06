package ru.gadjini.reminder.service.friendship;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTimeFormats;
import ru.gadjini.reminder.util.TimeCreator;
import ru.gadjini.reminder.util.UserUtils;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class FriendshipMessageBuilder {

    private LocalisationService localisationService;

    private TimeCreator timeCreator;

    public FriendshipMessageBuilder(LocalisationService localisationService, TimeCreator timeCreator) {
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    public String getFriendDetails(TgUser friend, Locale locale) {
        return getFriendDetails(friend, null, locale);
    }

    public String getFriendDetailsWithFooterCode(TgUser friend, String footerCode, Locale locale) {
        return getFriendDetails(friend, localisationService.getCurrentLocaleMessage(footerCode), locale);
    }

    public String getFriendDetails(TgUser friend, String footer, Locale locale) {
        StringBuilder message = new StringBuilder();

        message.append(UserUtils.userLink(friend)).append("\n\n");
        message.append(localisationService.getCurrentLocaleMessage(MessagesProperties.TIMEZONE, new Object[] {
                friend.getZone().getDisplayName(TextStyle.FULL, locale),
                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(timeCreator.zonedDateTimeNow(friend.getZone()))
        }));

        if (StringUtils.isNotBlank(footer)) {
            message.append("\n\n").append(footer);
        }

        return message.toString();
    }


    public String getFriendsList(List<TgUser> items, String emptyCode, String footer) {
        if (items.isEmpty()) {
            return localisationService.getCurrentLocaleMessage(emptyCode);
        }
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (TgUser friend : items) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append("<b>").append(i++).append("</b>) ").append(UserUtils.userLink(friend));
        }
        if (StringUtils.isNotBlank(footer)) {
            message.append("\n\n").append(localisationService.getCurrentLocaleMessage(footer));
        }

        return message.toString();
    }
}
