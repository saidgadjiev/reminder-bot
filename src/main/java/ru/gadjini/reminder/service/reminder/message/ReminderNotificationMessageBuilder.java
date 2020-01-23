package ru.gadjini.reminder.service.reminder.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.reminder.time.ReminderNotificationTimeBuilder;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeCreator;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReminderNotificationMessageBuilder {

    private MessageBuilder messageBuilder;

    private ReminderNotificationTimeBuilder reminderNotificationTimeBuilder;

    private ReminderMessageBuilder reminderMessageBuilder;

    private LocalisationService localisationService;

    private TimeCreator timeCreator;

    @Autowired
    public ReminderNotificationMessageBuilder(MessageBuilder messageBuilder, ReminderNotificationTimeBuilder reminderNotificationTimeBuilder,
                                              ReminderMessageBuilder reminderMessageBuilder, LocalisationService localisationService, TimeCreator timeCreator) {
        this.messageBuilder = messageBuilder;
        this.reminderNotificationTimeBuilder = reminderNotificationTimeBuilder;
        this.reminderMessageBuilder = reminderMessageBuilder;
        this.localisationService = localisationService;
        this.timeCreator = timeCreator;
    }

    public String getReminderNotificationForReceiver(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (itsTime) {
            return messageBuilder.getItsTimeReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId(), nextRemindAt));
        } else {
            return messageBuilder.getReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getReceiverId()));
        }
    }

    public String getReminderNotificationMySelf(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (itsTime) {
            return messageBuilder.getItsTimeReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId(), nextRemindAt));
        } else {
            return messageBuilder.getReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, reminder.getCreatorId()));
        }
    }

    public String getReminderTimeMessage(ReminderNotification reminderNotification) {
        if (reminderNotification.getType().equals(ReminderNotification.Type.ONCE)) {
            return reminderNotificationTimeBuilder.time(reminderNotification);
        } else {
            StringBuilder message = new StringBuilder(reminderNotificationTimeBuilder.time(reminderNotification));

            ZonedDateTime nextRemindAt = JodaTimeUtils.plus(reminderNotification.getLastReminderAt().withZoneSameInstant(reminderNotification.getReminder().getReceiver().getZone()), reminderNotification.getDelayTime());
            message.append("\n").append(messageBuilder.getNextReminderNotificationAt(nextRemindAt));

            return message.toString();
        }
    }

    public String getReminderNotifications(List<ReminderNotification> reminderNotifications) {
        if (reminderNotifications.isEmpty()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTIFICATION_NOT_EXISTS);
        }
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (ReminderNotification reminderNotification : reminderNotifications) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(reminderNotificationTimeBuilder.time(reminderNotification));
        }

        return message.toString();
    }

    public String getUserReminderNotifications(List<UserReminderNotification> userReminderNotifications) {
        if (userReminderNotifications.isEmpty()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTIFICATION_NOT_EXISTS);
        }
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(reminderNotificationTimeBuilder.time(userReminderNotification.withZone(timeCreator, userReminderNotification.getUser().getZone())));
        }

        return message.toString();
    }

}
