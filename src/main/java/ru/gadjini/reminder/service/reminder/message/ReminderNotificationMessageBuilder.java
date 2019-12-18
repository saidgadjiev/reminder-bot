package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.message.MessageBuilder;
import ru.gadjini.reminder.service.reminder.message.ReminderMessageBuilder;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReminderNotificationMessageBuilder {

    private MessageBuilder messageBuilder;

    private TimeBuilder timeBuilder;

    private ReminderMessageBuilder reminderMessageBuilder;

    private LocalisationService localisationService;

    @Autowired
    public ReminderNotificationMessageBuilder(MessageBuilder messageBuilder, TimeBuilder timeBuilder, ReminderMessageBuilder reminderMessageBuilder, LocalisationService localisationService) {
        this.messageBuilder = messageBuilder;
        this.timeBuilder = timeBuilder;
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.localisationService = localisationService;
    }

    public String getReminderNotificationForReceiver(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (itsTime) {
            return messageBuilder.getItsTimeReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()));
        } else {
            return messageBuilder.getReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()));
        }
    }

    public String getReminderNotificationMySelf(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (itsTime) {
            return messageBuilder.getItsTimeReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId()));
        } else {
            return messageBuilder.getReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId()));
        }
    }

    public String getReminderTimeMessage(ReminderNotification reminderNotification) {
        if (reminderNotification.getType().equals(ReminderNotification.Type.ONCE)) {
            return timeBuilder.time(reminderNotification);
        } else {
            StringBuilder message = new StringBuilder(timeBuilder.time(reminderNotification));

            ZonedDateTime nextRemindAt = JodaTimeUtils.plus(reminderNotification.getLastReminderAt().withZoneSameInstant(reminderNotification.getReminder().getReceiver().getZone()), reminderNotification.getDelayTime());
            message.append("\n").append(messageBuilder.getNextReminderNotificationAt(nextRemindAt));

            return message.toString();
        }
    }

    public String getUserReminderNotifications(List<UserReminderNotification> userReminderNotifications) {
        if (userReminderNotifications.isEmpty()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_USER_REMINDER_NOTIFICATION_NOT_EXISTS);
        }
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(timeBuilder.time(userReminderNotification));
        }

        return message.toString();
    }

}
