package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.ZonedDateTimeValidationContext;
import ru.gadjini.reminder.util.DateTimeService;

@Service
public class PastTimeValidator implements Validator<ZonedDateTimeValidationContext> {

    private LocalisationService localisationService;

    private DateTimeService timeCreator;

    @Autowired
    public PastTimeValidator(LocalisationService localisationService, DateTimeService timeCreator) {
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.PAST_TIME_VALIDATOR;
    }

    @Override
    public void validate(ZonedDateTimeValidationContext validationContext) {
        if (validationContext.dateTime().isBefore(timeCreator.zonedDateTimeNow(validationContext.dateTime().getZone()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_PAST_TIME, validationContext.locale()));
        }
    }
}
