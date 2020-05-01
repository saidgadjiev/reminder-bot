package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.TimeValidationContext;

@Component
public class ChallengeTimeValidator implements Validator<TimeValidationContext> {

    private LocalisationService localisationService;

    @Autowired
    public ChallengeTimeValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.CHALLENGE_TIME;
    }

    @Override
    public void validate(TimeValidationContext validationContext) {
        if (validationContext.time().isRepeatTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_CHALLENGE_TIME, validationContext.locale()));
        }
        if (validationContext.time().isOffsetTime() && !validationContext.time().getOffsetTime().getType().equals(OffsetTime.Type.FOR)) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_CHALLENGE_TIME, validationContext.locale()));
        }
    }
}
