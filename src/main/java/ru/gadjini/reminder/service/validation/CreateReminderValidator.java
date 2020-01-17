package ru.gadjini.reminder.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Friendship;
import ru.gadjini.reminder.domain.time.FixedTime;
import ru.gadjini.reminder.domain.time.OffsetTime;
import ru.gadjini.reminder.domain.time.Time;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.friendship.FriendshipService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Service
public class CreateReminderValidator implements Validator {

    private LocalisationService localisationService;

    private FriendshipService friendshipService;

    @Autowired
    public CreateReminderValidator(LocalisationService localisationService, FriendshipService friendshipService) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
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
        validate(validationContext.reminderRequest().getTime());
    }

    private void validate(Time time) {
        if (time.isFixedTime()) {
            validate(time.getFixedTime());
        } else if (time.isOffsetTime()) {
            validate(time.getOffsetTime());
        }
    }

    private void validate(DateTime dateTime) {
        if (!dateTime.hasTime()) {
            if (dateTime.date().isBefore(LocalDate.now(dateTime.getZoneId()))) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
            }
        } else {
            validate(dateTime.toZonedDateTime());
        }
    }

    private void validate(ZonedDateTime dateTime) {
        if (dateTime.isBefore(TimeUtils.now(dateTime.getZone()))) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validate(OffsetTime offsetTime) {
        if (offsetTime.getType() != OffsetTime.Type.AFTER) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }
    }

    private void validate(FixedTime fixedTime) {
        if (fixedTime.getType() != FixedTime.Type.AT) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_FORMAT));
        }

        validate(fixedTime.getDateTime());
    }

    private void checkFriendShip(int userId, String receiverName) {
        if (!friendshipService.existFriendship(userId, receiverName, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }

    private void checkFriendShip(int userId, int receiverId) {
        if (!friendshipService.existFriendship(userId, receiverId, Friendship.Status.ACCEPTED)) {
            throw new UserException(localisationService.getMessage(localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND)));
        }
    }
}
