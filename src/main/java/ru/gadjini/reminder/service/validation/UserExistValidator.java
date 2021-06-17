package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.UserValidationContext;

import java.util.Locale;

@Component
public class UserExistValidator implements Validator<UserValidationContext> {

    private TgUserService userService;

    private LocalisationService localisationService;

    @Autowired
    public UserExistValidator(TgUserService userService, LocalisationService localisationService) {
        this.userService = userService;
        this.localisationService = localisationService;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.USER_EXIST;
    }

    @Override
    public void validate(UserValidationContext validationContext) {
        if (validationContext.username() != null) {
            checkExists(validationContext.username(), validationContext.locale());
        } else if (validationContext.userId() != null) {
            checkExists(validationContext.userId(), validationContext.locale());
        }
    }

    private void checkExists(String username, Locale locale) {
        boolean exists = userService.isExists(username);

        if (!exists) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_NOT_EXISTS, locale));
        }
    }

    private void checkExists(long userId, Locale locale) {
        boolean exists = userService.isExists(userId);

        if (!exists) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_USER_NOT_EXISTS, locale));
        }
    }
}
