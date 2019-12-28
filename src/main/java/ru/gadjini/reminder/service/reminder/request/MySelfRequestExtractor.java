package ru.gadjini.reminder.service.reminder.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.TgUser;
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
    public ReminderRequest extract(String text, Integer receiverId) {
        if (!text.startsWith(TgUser.USERNAME_START) && receiverId == null) {
            ZoneId zoneId = tgUserService.getTimeZone(securityService.getAuthenticatedUser().getId());

            try {
                return requestParser.parseRequest(text, zoneId);
            } catch (ParseException ex) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        }

        return super.extract(text, receiverId);
    }
}
