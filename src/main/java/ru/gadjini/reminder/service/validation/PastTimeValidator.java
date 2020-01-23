package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.util.TimeUtils;

@Service
public class PastTimeValidator implements Validator {

    private LocalisationService localisationService;

    @Autowired
    public PastTimeValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.PAST_TIME_VALIDATOR;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        if (validationContext.dateTime().isBefore(TimeUtils.zonedDateTimeNow(validationContext.dateTime().getZone()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_PAST_TIME));
        }
    }
}
