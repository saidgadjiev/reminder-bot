package ru.gadjini.reminder.service.reminder;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.RemindAtCandidate;
import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.UpdateSeries;

@Service
public class RestoreReminderService {

    private ReminderNotificationService reminderNotificationService;

    private RepeatReminderBusinessService repeatReminderBusinessService;

    private DateTimeService timeCreator;

    @Autowired
    public RestoreReminderService(ReminderNotificationService reminderNotificationService,
                                  RepeatReminderBusinessService repeatReminderBusinessService,
                                  DateTimeService timeCreator) {
        this.reminderNotificationService = reminderNotificationService;
        this.repeatReminderBusinessService = repeatReminderBusinessService;
        this.timeCreator = timeCreator;
    }

    public boolean isNeedRestore(Reminder reminder) {
        if (reminder.isRepeatableWithTime()) {
            return isNeedRestoreRepeatableReminder(reminder);
        } else {
            return isNeedRestoreStandardReminder(reminder);
        }
    }

    @Transactional
    public void restore(Reminder reminder) {
        if (reminder.isRepeatableWithTime()) {
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
                if (repeatReminderBusinessService.isNeedUpdateNextRemindAt(reminder, reminderNotification)) {
                    RemindAtCandidate nextRemindAtCandidate = repeatReminderBusinessService.getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtsInReceiverZone(timeCreator), reminder.getCurrRepeatIndex());
                    DateTime nextRemindAt = nextRemindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC);
                    repeatReminderBusinessService.updateNextRemindAtAndSeries(reminder.getId(), UpdateSeries.NONE, nextRemindAtCandidate.getCurrentSeriesToComplete(), nextRemindAtCandidate.getIndex(), nextRemindAt);
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
                reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), timeCreator.localDateTimeNowWithMinutes());
            }
        }
    }

    private ZonedDateTime getNextLastRemindAt(ZonedDateTime lastRemindAt, Period period) {
        ZonedDateTime now = JodaTimeUtils.minus(timeCreator.zonedDateTimeNow(lastRemindAt.getZone()), period);

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

        if (reminderNotification.getType() == ReminderNotification.Type.ONCE) {
            return reminderNotification.getFixedTime().isBefore(timeCreator.zonedDateTimeNow());
        } else {
            return timeCreator.zonedDateTimeNow(reminderNotification.getLastReminderAt().getZone()).isAfter(JodaTimeUtils.plus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime()));
        }
    }

    private boolean isNeedRestoreStandardReminder(Reminder reminder) {
        return reminder.getReminderNotifications().size() > 1;
    }
}
