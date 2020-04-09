package ru.gadjini.reminder.service.reminder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.ParseException;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.parser.RequestParser;

import java.time.ZoneId;
import java.util.Locale;

@Service
public class TimeRequestService {

    private LocalisationService localisationService;

    private RequestParser requestParser;

    @Autowired
    public TimeRequestService(LocalisationService localisationService, RequestParser requestParser) {
        this.localisationService = localisationService;
        this.requestParser = requestParser;
    }

    public Time parseTime(String text, ZoneId zoneId, Locale locale) {
        try {
            return requestParser.parseTime(text, zoneId, locale);
        } catch (ParseException ex) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_BAD_TIME_FORMAT, locale));
        }
    }
}
