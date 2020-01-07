package ru.gadjini.reminder.service.reminder;

import com.google.inject.Inject;
import org.joda.time.Period;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.ZonedDateTime;

@Service
public class RestoreReminderService {

    private ReminderNotificationService reminderNotificationService;

    private RepeatReminderService repeatReminderService;

    @Inject
    public RestoreReminderService(ReminderNotificationService reminderNotificationService, RepeatReminderService repeatReminderService) {
        this.reminderNotificationService = reminderNotificationService;
        this.repeatReminderService = repeatReminderService;
    }

    public boolean isNeedRestore(Reminder reminder) {
        if (reminder.isRepeatable()) {
            return isNeedRestoreRepeatableReminder(reminder);
        } else {
            return isNeedRestoreStandardReminder(reminder);
        }
    }

    @Transactional
    public void restore(Reminder reminder) {
        if (reminder.isRepeatable()) {
            restoreRepeatableReminder(reminder);
        } else {
            restoreStandardReminder(reminder);
        }
    }

    private void restoreRepeatableReminder(Reminder reminder) {
        for (ReminderNotification reminderNotification : reminder.getReminderNotifications()) {
            if (reminderNotification.getType().equals(ReminderNotification.Type.ONCE)) {
                reminderNotificationService.deleteReminderNotification(reminderNotification.getId());
            } else {
                if (repeatReminderService.isNeedUpdateNextRemindAt(reminder, reminderNotification)) {
                    DateTime nextRemindAt = repeatReminderService.getNextRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAt());
                    repeatReminderService.updateNextRemindAt(reminder.getId(), nextRemindAt, RepeatReminderService.UpdateSeries.NONE);
                    reminder.setRemindAt(nextRemindAt);
                }
                ZonedDateTime restoredLastRemindAt = getNextLastRemindAt(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
                reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), restoredLastRemindAt.toLocalDateTime());
            }
        }
    }

    private void restoreStandardReminder(Reminder reminder) {
        for (ReminderNotification reminderNotification : reminder.getReminderNotifications()) {
            if (reminderNotification.getType().equals(ReminderNotification.Type.ONCE)) {
                reminderNotificationService.deleteReminderNotification(reminderNotification.getId());
            } else {
                reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), TimeUtils.now());
            }
        }
    }

    private ZonedDateTime getNextLastRemindAt(ZonedDateTime lastRemindAt, Period period) {
        ZonedDateTime now = JodaTimeUtils.minus(ZonedDateTime.now(lastRemindAt.getZone()), period);

        while (now.isAfter(lastRemindAt)) {
            lastRemindAt = JodaTimeUtils.plus(lastRemindAt, period);
        }

        return lastRemindAt;
    }

    private boolean isNeedRestoreRepeatableReminder(Reminder reminder) {
        if (reminder.getReminderNotifications().size() > 1) {
            return true;
        }
        ReminderNotification reminderNotification = reminder.getReminderNotifications().get(0);

        return TimeUtils.now(reminderNotification.getLastReminderAt().getZone()).isAfter(JodaTimeUtils.plus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime()));
    }

    private boolean isNeedRestoreStandardReminder(Reminder reminder) {
        return reminder.getReminderNotifications().size() > 1;
    }
}
