package ru.gadjini.reminder.service.friendship;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTimeFormats;
import ru.gadjini.reminder.util.DateTimeService;
import ru.gadjini.reminder.util.UserUtils;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class FriendshipMessageBuilder {

    private LocalisationService localisationService;

    private DateTimeService timeCreator;

    public FriendshipMessageBuilder(LocalisationService localisationService, DateTimeService timeCreator) {
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    public String getFriendDetails(TgUser friend, Locale locale) {
        return getFriendDetails(friend, null, locale);
    }

    public String getFriendDetailsWithFooterCode(TgUser friend, String footerCode, Locale locale) {
        return getFriendDetails(friend, localisationService.getMessage(footerCode, locale), locale);
    }

    public String getFriendDetails(TgUser friend, String footer, Locale locale) {
        StringBuilder message = new StringBuilder();

        message.append(UserUtils.userLink(friend)).append("\n\n");
        message.append(localisationService.getMessage(MessagesProperties.TIMEZONE, new Object[] {
                friend.getZone().getDisplayName(TextStyle.FULL, locale),
                DateTimeFormats.TIMEZONE_LOCAL_TIME_FORMATTER.format(timeCreator.zonedDateTimeNow(friend.getZone()))
        }, locale));

        if (StringUtils.isNotBlank(footer)) {
            message.append("\n\n").append(footer);
        }

        return message.toString();
    }

    public String getFriendsList(List<TgUser> items, String emptyCode, String headerCode, String footerCode, Locale locale) {
        if (items.isEmpty()) {
            return localisationService.getMessage(emptyCode, locale);
        }
        StringBuilder message = new StringBuilder();
        if (StringUtils.isNotBlank(headerCode)) {
            message.append(localisationService.getMessage(headerCode, locale));
        }

        int i = 1;
        for (TgUser friend : items) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append("<b>").append(i++).append("</b>) ").append(UserUtils.userLink(friend));
        }
        if (StringUtils.isNotBlank(footerCode)) {
            message.append("\n\n").append(localisationService.getMessage(footerCode, locale));
        }

        return message.toString();
    }
}
