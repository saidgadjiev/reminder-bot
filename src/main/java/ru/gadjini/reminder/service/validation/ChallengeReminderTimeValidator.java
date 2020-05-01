package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.TimeValidationContext;

@Component
public class ChallengeReminderTimeValidator implements Validator<TimeValidationContext> {

    private LocalisationService localisationService;

    @Autowired
    public ChallengeReminderTimeValidator(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return null;
    }

    @Override
    public void validate(TimeValidationContext validationContext) {
        if (!validationContext.time().isRepeatTime()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_INCORRECT_REMINDER_TYPE_IN_CHALLENGE, validationContext.locale()));
        }
    }
}
