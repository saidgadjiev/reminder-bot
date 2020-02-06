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
        ZoneId zoneId = context.getReceiverZone();

        if (zoneId == null) {
            zoneId = tgUserService.getTimeZone(context.getUser().getId());
        }

        try {
            return requestParser.parseRequest(context.getText(), zoneId, localisationService.getCurrentLocale(context.getUser().getLanguageCode()));
        } catch (ParseException ex) {
            throw new UserException(getMessage(context.getText(), context.isVoice(), localisationService.getCurrentLocale(context.getUser().getLanguageCode())));
        }
    }

    private String getMessage(String text, boolean voice, Locale locale) {
        return voice ? localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT_VOICE, new Object[]{text}, locale) : localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT, locale);
    }
}
