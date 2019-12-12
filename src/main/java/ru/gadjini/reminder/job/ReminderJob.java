package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.service.reminder.ReminderMessageSender;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.RepeatReminderService;
import ru.gadjini.reminder.service.reminder.RestoreReminderService;
import ru.gadjini.reminder.service.reminder.remindertime.ReminderTimeService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class ReminderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderJob.class);

    private ReminderService reminderService;

    private ReminderTimeService reminderTimeService;

    private ReminderMessageSender reminderMessageSender;

    private RepeatReminderService repeatReminderService;

    private RestoreReminderService restoreReminderService;

    @Autowired
    public ReminderJob(ReminderService reminderService,
                       ReminderTimeService reminderTimeService,
                       ReminderMessageSender reminderMessageSender,
                       RepeatReminderService repeatReminderService,
                       RestoreReminderService restoreReminderService) {
        this.reminderService = reminderService;
        this.reminderTimeService = reminderTimeService;
        this.reminderMessageSender = reminderMessageSender;
        this.repeatReminderService = repeatReminderService;
        this.restoreReminderService = restoreReminderService;
    }

    // @Scheduled(cron = "0 0 0 * * *")
    //@Scheduled(fixedDelay = 1000)
    public void deleteCompletedReminders() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = reminderService.deleteCompletedReminders(now);

        LOGGER.debug("Delete " + deleted + " completed reminders at " + now);
    }

    @Scheduled(fixedDelay = 1000)
    public void sendReminders() {
        List<Reminder> reminders = reminderService.getRemindersWithReminderTimes(LocalDateTime.now().minusSeconds(15).withNano(0), 30);

        for (Reminder reminder : reminders) {
            if (restoreReminderService.isNeedRestore(reminder)) {
                restoreReminderService.restore(reminder);
                sendReminderRestored(reminder);
            } else {
                sendReminder(reminder);
            }
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
        reminderMessageSender.sendRemindMessage(reminder, reminder.getReminderTimes().stream().anyMatch(ReminderTime::isItsTime), null);
    }

    private void sendRepeatableReminderRestored(Reminder reminder) {
        reminderMessageSender.sendRemindMessage(
                reminder,
                reminder.getReminderTimes().stream().anyMatch(ReminderTime::isItsTime),
                reminder.getRemindAtInReceiverZone()
        );
    }

    private void sendReminder(Reminder reminder) {
        for (ReminderTime reminderTime : reminder.getReminderTimes()) {
            switch (reminderTime.getType()) {
                case ONCE:
                    sendOnceReminder(reminder, reminderTime);
                    break;
                case REPEAT:
                    if (reminder.isRepeatable()) {
                        sendRepeatReminderTimeForRepeatableReminder(reminder, reminderTime);
                    } else {
                        sendRepeatReminderTime(reminder, reminderTime);
                    }
                    break;
            }
        }
    }

    private void sendOnceReminder(Reminder reminder, ReminderTime reminderTime) {
        reminderMessageSender.sendRemindMessage(reminder, reminderTime.isItsTime(), null);
        reminderTimeService.deleteReminderTime(reminderTime.getId());
    }

    private void sendRepeatReminderTime(Reminder reminder, ReminderTime reminderTime) {
        reminderMessageSender.sendRemindMessage(reminder, reminderTime.isItsTime(), null);

        reminderTimeService.updateLastRemindAt(reminderTime.getId(), TimeUtils.now());
    }

    private void sendRepeatReminderTimeForRepeatableReminder(Reminder reminder, ReminderTime reminderTime) {
        DateTime nextRemindAt = reminder.getRemindAtInReceiverZone();

        if (reminderTime.isItsTime()) {
            nextRemindAt = repeatReminderService.getNextRemindAt(reminder.getRemindAt(), reminder.getRepeatRemindAt());
            repeatReminderService.updateNextRemindAt(reminder.getId(), nextRemindAt);
            reminder.setRemindAt(nextRemindAt);
        }

        reminderMessageSender.sendRemindMessage(reminder, reminderTime.isItsTime(), nextRemindAt);

        ZonedDateTime nextLastRemindAt = JodaTimeUtils.plus(reminderTime.getLastReminderAt(), reminderTime.getDelayTime());
        reminderTimeService.updateLastRemindAt(reminderTime.getId(), nextLastRemindAt.toLocalDateTime());
    }
}
