package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.validation.context.ReminderRequestValidationContext;
import ru.gadjini.reminder.service.validation.context.TimeValidationContext;

import java.util.Locale;
import java.util.Objects;

@Service
public class CreateReminderValidator implements Validator<ReminderRequestValidationContext> {

    private LocalisationService localisationService;

    private FriendshipService friendshipService;

    private ReminderTimeValidator reminderTimeValidator;

    @Autowired
    public CreateReminderValidator(LocalisationService localisationService, FriendshipService friendshipService, ReminderTimeValidator reminderTimeValidator) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
        this.reminderTimeValidator = reminderTimeValidator;
    }

    @Override
    public ValidatorType event() {
        return ValidatorType.CREATE_REMINDER;
    }

    @Override
    public void validate(ReminderRequestValidationContext validationContext) {
        if (validationContext.reminderRequest().getReceiverName() != null) {
            checkFriendShip(validationContext.reminderRequest().getCreatorId(), validationContext.reminderRequest().getReceiverName(), validationContext.reminderRequest().getLocale());
        } else if (!Objects.equals(validationContext.reminderRequest().getReceiverId(), validationContext.reminderRequest().getCreatorId())) {
            checkFriendShip(validationContext.reminderRequest().getCreatorId(), validationContext.reminderRequest().getReceiverId(), validationContext.reminderRequest().getLocale());
        }
        reminderTimeValidator.validate(new TimeValidationContext().time(validationContext.reminderRequest().getTime()).locale(validationContext.reminderRequest().getLocale()));
    }

    private void checkFriendShip(int userId, String receiverName, Locale locale) {
        if (!friendshipService.existFriendship(userId, receiverName, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND, locale));
        }
    }

    private void checkFriendShip(int userId, int receiverId, Locale locale) {
        if (!friendshipService.existFriendship(userId, receiverId, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND, locale));
        }
    }
}
