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

import java.util.Locale;

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
        Integer receiverId = context.getReceiverId();

        if (receiverId != null) {
            TgUser receiver = context.getReceiver();
            if (receiver == null) {
                receiver = tgUserService.getByUserId(receiverId);
            }

            try {
                ReminderRequest reminderRequest = requestParser.parseRequest(context.getText(), receiver.getZone(), receiver.getLocale());
                reminderRequest.setReceiverId(receiverId);
                reminderRequest.setLocale(receiver.getLocale());

                return reminderRequest;
            } catch (ParseException ex) {
                throw new UserException(getMessage(context.getText(), context.isVoice(), receiver.getLocale()));
            }
        }

        return super.extract(context);
    }

    private String getMessage(String text, boolean voice, Locale locale) {
        return voice ? localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT_VOICE, new Object[] {text}, locale) : localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT, locale);
    }
}
