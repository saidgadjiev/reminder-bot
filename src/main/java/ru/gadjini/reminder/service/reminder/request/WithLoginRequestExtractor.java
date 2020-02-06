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
import java.util.Locale;

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
            Locale locale = tgUserService.getLocale(context.getUser().getId());
            if (text.indexOf(' ') == -1) {
                throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT, locale));
            }
            String username = text.substring(1, text.indexOf(' '));
            ZoneId zoneId = tgUserService.getTimeZone(username);
            text = text.substring(username.length() + 2);

            try {
                ReminderRequest reminderRequest = requestParser.parseRequest(text, zoneId, locale);
                reminderRequest.setReceiverName(username);
                reminderRequest.setLocale(locale);

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT, locale));
            }
        }

        return super.extract(context);
    }
}
