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
import java.util.Locale;

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
            return messageBuilder.getItsTimeReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getReceiverId()).nextRemindAt(nextRemindAt).remindNotification(true)), reminder.getReceiver().getLocale());
        } else {
            return messageBuilder.getReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getReceiverId()).remindNotification(true)), reminder.getReceiver().getLocale());
        }
    }

    public String getReminderNotificationMySelf(Reminder reminder, boolean itsTime, DateTime nextRemindAt) {
        if (itsTime) {
            return messageBuilder.getItsTimeReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getCreatorId()).nextRemindAt(nextRemindAt).remindNotification(true)), reminder.getCreator().getLocale());
        } else {
            return messageBuilder.getReminderNotification(reminderMessageBuilder.getReminderMessage(reminder, new ReminderMessageBuilder.ReminderMessageConfig().receiverId(reminder.getCreatorId()).remindNotification(true)), reminder.getReceiver().getLocale());
        }
    }

    public String getReminderTimesMessage(List<ReminderNotification> reminderNotifications, Locale locale) {
        StringBuilder message = new StringBuilder();

        for (ReminderNotification notification : reminderNotifications) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(getReminderTimeMessage(notification, locale));
        }

        return message.toString();
    }

    public String getReminderTimeMessage(ReminderNotification reminderNotification, Locale locale) {
        if (reminderNotification.getType().equals(ReminderNotification.Type.ONCE)) {
            return reminderNotificationTimeBuilder.time(reminderNotification, locale);
        } else {
            StringBuilder message = new StringBuilder(reminderNotificationTimeBuilder.time(reminderNotification, locale));

            ZonedDateTime nextRemindAt = JodaTimeUtils.plus(reminderNotification.getLastReminderAt().withZoneSameInstant(reminderNotification.getReminder().getReceiver().getZone()), reminderNotification.getDelayTime());
            message.append("\n").append(messageBuilder.getNextReminderNotificationAt(nextRemindAt, locale));

            return message.toString();
        }
    }

    public String getReminderNotifications(List<ReminderNotification> reminderNotifications, Locale locale) {
        if (reminderNotifications.isEmpty()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTIFICATION_NOT_EXISTS, locale);
        }
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (ReminderNotification reminderNotification : reminderNotifications) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(reminderNotificationTimeBuilder.time(reminderNotification, locale));
        }

        return message.toString();
    }

    public String getUserReminderNotifications(List<UserReminderNotification> userReminderNotifications, Locale locale) {
        if (userReminderNotifications.isEmpty()) {
            return localisationService.getMessage(MessagesProperties.MESSAGE_REMINDER_NOTIFICATION_NOT_EXISTS, locale);
        }
        StringBuilder message = new StringBuilder();

        int i = 1;
        for (UserReminderNotification userReminderNotification : userReminderNotifications) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(i++).append(") ").append(reminderNotificationTimeBuilder.time(timeCreator.withZone(userReminderNotification, userReminderNotification.getUser().getZone())));
        }

        return message.toString();
    }

}
