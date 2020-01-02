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
import ru.gadjini.reminder.service.security.SecurityService;

import java.time.ZoneId;

@Component
public class MySelfRequestExtractor extends BaseRequestExtractor {

    private SecurityService securityService;

    private TgUserService tgUserService;

    private LocalisationService localisationService;

    private RequestParser requestParser;

    @Autowired
    public MySelfRequestExtractor(SecurityService securityService, TgUserService tgUserService,
                                  LocalisationService localisationService, RequestParser requestParser) {
        this.securityService = securityService;
        this.tgUserService = tgUserService;
        this.localisationService = localisationService;
        this.requestParser = requestParser;
    }

    @Override
    public ReminderRequest extract(ReminderRequestContext context) {
        ZoneId zoneId = context.getReceiverZone();

        if (zoneId == null) {
            zoneId = tgUserService.getTimeZone(securityService.getAuthenticatedUser().getId());
        }

        try {
            return requestParser.parseRequest(context.getText(), zoneId);
        } catch (ParseException ex) {
            throw new UserException(getMessage(context.getText(), context.isVoice()));
        }
    }

    private String getMessage(String text, boolean voice) {
        return voice ? localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT_VOICE, new Object[] {text}) : localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT);
    }
}
