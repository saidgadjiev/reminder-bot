package ru.gadjini.reminder.service.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.service.FriendshipService;
import ru.gadjini.reminder.service.LocalisationService;

import java.time.LocalDateTime;

@Service
public class ValidationService {

    private LocalisationService localisationService;

    private FriendshipService friendshipService;

    @Autowired
    public ValidationService(LocalisationService localisationService, FriendshipService friendshipService) {
        this.localisationService = localisationService;
        this.friendshipService = friendshipService;
    }

    public ErrorBag validate(ReminderRequest reminderRequest) {
        ErrorBag errorBag = new ErrorBag();

        if (reminderRequest.getRemindAt().isBefore(LocalDateTime.now())) {
            errorBag.set("remindAt", localisationService.getMessage(MessagesProperties.MESSAGE_BAD_REMIND_AT));

            return errorBag;
        }
        if (!reminderRequest.isForMe()) {
            String receiverName = reminderRequest.getReceiverName();
            boolean friend;
            if (StringUtils.isNotBlank(receiverName)) {
                friend = friendshipService.isFriend(receiverName);
            } else {
                friend = friendshipService.isFriend(reminderRequest.getReceiverId());
            }

            if (!friend) {
                errorBag.set("notFriend", localisationService.getMessage(MessagesProperties.MESSAGE_REMIND_NOT_FRIEND));

                return errorBag;
            }
        }

        return errorBag;
    }
}
