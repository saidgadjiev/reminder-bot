package ru.gadjini.reminder.service.reminder.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;
import ru.gadjini.reminder.service.parser.reminder.parser.ReminderRequest;

import java.time.ZoneId;
import java.util.Locale;

@Component
public class MySelfRequestExtractor extends BaseRequestExtractor {

    private TgUserService tgUserService;

    private LocalisationService localisationService;

    private RequestParser requestParser;

    @Autowired
    public MySelfRequestExtractor(TgUserService tgUserService, LocalisationService localisationService, RequestParser requestParser) {
        this.tgUserService = tgUserService;
        this.localisationService = localisationService;
        this.requestParser = requestParser;
    }

    @Override
    public ReminderRequest extract(ReminderRequestContext context) {
        ZoneId zoneId = context.receiverZoneId();

        if (zoneId == null) {
            zoneId = tgUserService.getTimeZone(context.user().getId());
        }

        Locale locale = context.locale();

        if (locale == null) {
            locale = tgUserService.getLocale(context.user().getId());
        }

        try {
            ReminderRequest reminderRequest = requestParser.parseRequest(context.text(), zoneId, locale);
            reminderRequest.setLocale(locale);

            return reminderRequest;
        } catch (ParseException ex) {
            throw new UserException(getMessage(context.text(), context.voice(), locale));
        }
    }

    private String getMessage(String text, boolean voice, Locale locale) {
        return voice ? localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT_VOICE, new Object[]{text}, locale) : localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT, locale);
    }
}
