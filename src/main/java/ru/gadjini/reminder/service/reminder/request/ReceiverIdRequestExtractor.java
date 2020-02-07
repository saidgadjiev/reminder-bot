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
import java.util.Objects;

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
    public ReminderRequest extract(ReminderRequestContext context) {
        Integer receiverId = context.receiverId();

        if (receiverId != null && !Objects.equals(receiverId, context.creator().getId())) {
            ZoneId zoneId = context.receiverZoneId();
            if (zoneId == null) {
                zoneId = tgUserService.getTimeZone(receiverId);
            }

            Locale locale = context.creatorLocale();
            if (locale == null) {
                locale = tgUserService.getLocale(context.creator().getId());
            }

            try {
                ReminderRequest reminderRequest = requestParser.parseRequest(context.text(), zoneId, locale);
                reminderRequest.setReceiverId(receiverId);
                reminderRequest.setLocale(locale);

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(getMessage(context.text(), context.voice(), locale));
            }
        }

        return super.extract(context);
    }

    private String getMessage(String text, boolean voice, Locale locale) {
        return voice ? localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT_VOICE, new Object[] {text}, locale) : localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT, locale);
    }
}
