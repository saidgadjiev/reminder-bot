package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.service.reminder.*;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class ReminderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderJob.class);

    private ReminderService reminderService;

    private ReminderNotificationService reminderNotificationService;

    private ReminderMessageSender reminderMessageSender;

    private ReminderNotificationMessageSender reminderNotificationMessageSender;

    private RepeatReminderService repeatReminderService;

    private RestoreReminderService restoreReminderService;

    @Autowired
    public ReminderJob(ReminderService reminderService,
                       ReminderNotificationService reminderNotificationService,
                       ReminderMessageSender reminderMessageSender,
                       ReminderNotificationMessageSender reminderNotificationMessageSender,
                       RepeatReminderService repeatReminderService,
                       RestoreReminderService restoreReminderService) {
        this.reminderService = reminderService;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderMessageSender = reminderMessageSender;
        this.reminderNotificationMessageSender = reminderNotificationMessageSender;
        this.repeatReminderService = repeatReminderService;
        this.restoreReminderService = restoreReminderService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    //@Scheduled(fixedDelay = 1000)
    public void deleteCompletedReminders() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = reminderService.deleteCompletedReminders(now);

        LOGGER.debug("Delete " + deleted + " completed reminders at " + now);
    }

    @Scheduled(fixedDelay = 1000)
    public void sendReminders() {
        try {
            List<Reminder> reminders = reminderService.getRemindersWithReminderTimes(LocalDateTime.now().minusSeconds(15).withNano(0), 30);

            for (Reminder reminder : reminders) {
                if (restoreReminderService.isNeedRestore(reminder)) {
                    restoreReminderService.restore(reminder);
                    sendReminderRestored(reminder);
                } else {
                    sendReminder(reminder);
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void sendReminderRestored(Reminder reminder) {
        if (reminder.isRepeatable()) {
            sendRepeatableReminderRestored(reminder);
        } else {
            sendStandardReminderRestored(reminder);
        }
    }

    private void sendStandardReminderRestored(Reminder reminder) {
        reminderNotificationMessageSender.sendRemindMessage(reminder, reminder.getReminderNotifications().stream().anyMatch(ReminderNotification::isItsTime));
    }

    private void sendRepeatableReminderRestored(Reminder reminder) {
        reminderNotificationMessageSender.sendRemindMessage(
                reminder,
                reminder.getReminderNotifications().stream().anyMatch(ReminderNotification::isItsTime)
        );
    }

    private void sendReminder(Reminder reminder) {
        for (ReminderNotification reminderNotification : reminder.getReminderNotifications()) {
            try {
                switch (reminderNotification.getType()) {
                    case ONCE:
                        sendOnceReminder(reminder, reminderNotification);
                        break;
                    case REPEAT:
                        if (reminder.isRepeatable()) {
                            sendRepeatReminderTimeForRepeatableReminder(reminder, reminderNotification);
                        } else {
                            sendRepeatReminderTime(reminder, reminderNotification);
                        }
                        break;
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    private void sendOnceReminder(Reminder reminder, ReminderNotification reminderNotification) {
        reminderNotificationMessageSender.sendRemindMessage(reminder, reminderNotification.isItsTime());
        reminderNotificationService.deleteReminderTime(reminderNotification.getId());
    }

    private void sendRepeatReminderTime(Reminder reminder, ReminderNotification reminderNotification) {
        reminderNotificationMessageSender.sendRemindMessage(reminder, reminderNotification.isItsTime());

        reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), TimeUtils.now());
    }

    private void sendRepeatReminderTimeForRepeatableReminder(Reminder reminder, ReminderNotification reminderNotification) {
        DateTime nextRemindAt = reminder.getRemindAt();

        if (isNeedUpdateNextRemindAt(reminder, reminderNotification)) {
            nextRemindAt = repeatReminderService.getNextRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAt());
            repeatReminderService.updateNextRemindAt(reminder.getId(), nextRemindAt);
            reminder.setRemindAt(nextRemindAt);
        }

        reminderNotificationMessageSender.sendRemindMessage(reminder, reminderNotification.isItsTime(), nextRemindAt);

        ZonedDateTime nextLastRemindAt = JodaTimeUtils.plus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
        reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), nextLastRemindAt.toLocalDateTime());
    }

    private boolean isNeedUpdateNextRemindAt(Reminder reminder, ReminderNotification reminderNotification) {
        LocalDate now = LocalDate.now(reminder.getRemindAt().getZone());

        return reminderNotification.isItsTime() || (!reminder.getRemindAt().hasTime() && (reminder.getRemindAt().date().equals(now) || reminder.getRemindAt().date().isBefore(now)));
    }
}
