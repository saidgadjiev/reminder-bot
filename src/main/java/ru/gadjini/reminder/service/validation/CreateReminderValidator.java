package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;

@Service
public class CreateReminderValidator implements Validator {

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
    public void validate(ValidationContext validationContext) {
        if (validationContext.reminderRequest().getReceiverName() != null) {
            checkFriendShip(validationContext.currentUser().getId(), validationContext.reminderRequest().getReceiverName());
        } else if (validationContext.reminderRequest().getReceiverId() != null) {
            checkFriendShip(validationContext.currentUser().getId(), validationContext.reminderRequest().getReceiverId());
        }
        reminderTimeValidator.validate(new ValidationContext().time(validationContext.reminderRequest().getTime()));
    }

    private void checkFriendShip(int userId, String receiverName) {
        if (!friendshipService.existFriendship(userId, receiverName, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getCurrentLocaleMessage(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }

    private void checkFriendShip(int userId, int receiverId) {
        if (!friendshipService.existFriendship(userId, receiverId, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getCurrentLocaleMessage(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }
}
