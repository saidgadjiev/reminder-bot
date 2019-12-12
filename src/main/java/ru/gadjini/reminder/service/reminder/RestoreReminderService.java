package ru.gadjini.reminder.service.reminder;

import com.google.inject.Inject;
import org.joda.time.Period;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.ZonedDateTime;

@Service
public class RestoreReminderService {

    private ReminderTimeService reminderTimeService;

    private RepeatReminderService repeatReminderService;

    @Inject
    public RestoreReminderService(ReminderTimeService reminderTimeService, RepeatReminderService repeatReminderService) {
        this.reminderTimeService = reminderTimeService;
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
        for (ReminderTime reminderTime : reminder.getReminderTimes()) {
            if (reminderTime.getType().equals(ReminderTime.Type.ONCE)) {
                reminderTimeService.deleteReminderTime(reminderTime.getId());
            } else {
                if (reminderTime.isItsTime()) {
                    DateTime nextRemindAt = repeatReminderService.getNextRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAt());
                    repeatReminderService.updateNextRemindAt(reminder.getId(), nextRemindAt);
                    reminder.setRemindAt(nextRemindAt);
                }
                ZonedDateTime restoredLastRemindAt = getNextLastRemindAt(reminderTime.getLastReminderAt(), reminderTime.getDelayTime());
                reminderTimeService.updateLastRemindAt(reminderTime.getId(), restoredLastRemindAt.toLocalDateTime());
            }
        }
    }

    private void restoreStandardReminder(Reminder reminder) {
        for (ReminderTime reminderTime : reminder.getReminderTimes()) {
            if (reminderTime.getType().equals(ReminderTime.Type.ONCE)) {
                reminderTimeService.deleteReminderTime(reminderTime.getId());
            } else {
                reminderTimeService.updateLastRemindAt(reminderTime.getId(), TimeUtils.now());
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
        if (reminder.getReminderTimes().size() > 1) {
            return true;
        }
        ReminderTime reminderTime = reminder.getReminderTimes().get(0);

        return TimeUtils.now(reminderTime.getLastReminderAt().getZone()).isAfter(JodaTimeUtils.plus(reminderTime.getLastReminderAt(), reminderTime.getDelayTime()));
    }

    private boolean isNeedRestoreStandardReminder(Reminder reminder) {
        if (reminder.getReminderTimes().size() > 1) {
            return true;
        }
        return false;
    }
}
