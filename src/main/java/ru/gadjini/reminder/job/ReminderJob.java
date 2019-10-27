package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.service.ReminderMessageSender;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.ReminderTimeService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderJob.class);

    private ReminderService reminderService;

    private ReminderTimeService reminderTimeService;

    private ReminderMessageSender reminderMessageSender;

    @Autowired
    public ReminderJob(ReminderService reminderService,
                       ReminderTimeService reminderTimeService,
                       ReminderMessageSender reminderMessageSender) {
        this.reminderService = reminderService;
        this.reminderTimeService = reminderTimeService;
        this.reminderMessageSender = reminderMessageSender;
    }

    @Scheduled(fixedDelay = 1000)
    public void sendReminders() {
        List<Reminder> reminders = reminderService.getRemindersWithReminderTimes(LocalDateTime.now(), 30);

        for (Reminder reminder : reminders) {
            sendReminder(reminder);
        }
    }

    private void sendReminder(Reminder reminder) {
        for (ReminderTime reminderTime : reminder.getReminderTimes()) {
            switch (reminderTime.getType()) {
                case ONCE:
                    sendOnceReminder(reminder, reminderTime);
                    break;
                case REPEAT:
                    sendRepeatReminder(reminder, reminderTime);
                    break;
            }
        }
    }

    private void sendOnceReminder(Reminder reminder, ReminderTime reminderTime) {
        reminderMessageSender.sendRemindMessage(reminder);

        reminderTimeService.deleteReminderTime(reminderTime.getId());
        LOGGER.debug("Send once reminder");
    }

    private void sendRepeatReminder(Reminder reminder, ReminderTime reminderTime) {
        reminderMessageSender.sendRemindMessage(reminder);

        if (reminderTime.getLastReminderAt() == null) {
            reminderTimeService.updateLastRemindAt(reminderTime.getId(), reminder.getRemindAt().toLocalDateTime());
        } else {
            reminderTimeService.updateLastRemindAt(reminderTime.getId(), LocalDateTime.now());
        }
        LOGGER.debug("Send repeat reminder");
    }
}
