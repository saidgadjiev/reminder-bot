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

@Component
public class ReceiverIdRequestExtractor extends BaseRequestExtractor {

    private TgUserService tgUserService;

    private RequestParser requestParser;

    private LocalisationService localisationService;

    @Autowired
    public ReceiverIdRequestExtractor(TgUserService tgUserService, RequestParser requestParser, LocalisationService localisationService) {
        this.tgUserService = tgUserService;
        this.requestParser = requestParser;
        this.localisationService = localisationService;
    }

    @Override
    public ReminderRequest extract(String text, Integer receiverId, boolean voice) {
        if (receiverId != null) {
            ZoneId zone = tgUserService.getTimeZone(receiverId);

            try {
                ReminderRequest reminderRequest = requestParser.parseRequest(text, zone);
                reminderRequest.setReceiverId(receiverId);

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        }

        return super.extract(text, receiverId, voice);
    }
}
