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

import java.time.ZoneId;

@Component
public class WithLoginRequestExtractor extends BaseRequestExtractor {

    private TgUserService tgUserService;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    @Autowired
    public WithLoginRequestExtractor(TgUserService tgUserService, RequestParser requestParser, LocalisationService localisationService) {
        this.tgUserService = tgUserService;
        this.requestParser = requestParser;
        this.localisationService = localisationService;
    }

    @Override
    public ReminderRequest extract(ReminderRequestContext context) {
        String text = context.getText();

        if (text.startsWith(TgUser.USERNAME_START)) {
            if (text.indexOf(' ') == -1) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
            String username = text.substring(1, text.indexOf(' '));
            ZoneId zoneId = tgUserService.getTimeZone(username);
            text = text.substring(username.length() + 2);

            try {
                ReminderRequest reminderRequest = requestParser.parseRequest(text, zoneId);
                reminderRequest.setReceiverName(username);

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        }

        return super.extract(context);
    }
}
