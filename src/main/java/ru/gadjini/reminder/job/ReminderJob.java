package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.RestoreReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageSender;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService;
import ru.gadjini.reminder.service.reminder.repeat.RepeatReminderService;
import ru.gadjini.reminder.service.reminder.simple.ReminderService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.DateTimeService;
import ru.gadjini.reminder.util.JodaTimeUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.RemindAtCandidate;
import static ru.gadjini.reminder.service.reminder.repeat.RepeatReminderBusinessService.UpdateSeries;

@Component
public class ReminderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderJob.class);

    private ReminderService reminderService;

    private ReminderNotificationService reminderNotificationService;

    private ReminderNotificationMessageSender reminderNotificationMessageSender;

    private RepeatReminderService repeatReminderService;

    private RepeatReminderBusinessService repeatReminderBusinessService;

    private RestoreReminderService restoreReminderService;

    private MessageService messageService;

    private DateTimeService timeCreator;

    @Autowired
    public ReminderJob(ReminderService reminderService,
                       ReminderNotificationService reminderNotificationService,
                       ReminderNotificationMessageSender reminderNotificationMessageSender,
                       RepeatReminderService repeatReminderService,
                       RepeatReminderBusinessService repeatReminderBusinessService, RestoreReminderService restoreReminderService,
                       MessageService messageService, DateTimeService timeCreator) {
        this.reminderService = reminderService;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderNotificationMessageSender = reminderNotificationMessageSender;
        this.repeatReminderService = repeatReminderService;
        this.repeatReminderBusinessService = repeatReminderBusinessService;
        this.restoreReminderService = restoreReminderService;
        this.messageService = messageService;
        this.timeCreator = timeCreator;

        LOGGER.debug("Reminder job initialized and working");
    }

    @PostConstruct
    public void onStartup() {
        sendReminders();
        moveReminders();
    }

    //every hour
    @Scheduled(cron = "0 0 * * * *")
    public void moveReminders() {
        List<Reminder> overdueReminders = repeatReminderService.getOverdueRepeatReminders();

        for (Reminder reminder : overdueReminders) {
            repeatReminderBusinessService.autoSkip(reminder);
            if (reminder.getReceiverMessageId() != null) {
                messageService.deleteMessage(reminder.getReceiverId(), reminder.getReceiverMessageId());
            }

            LOGGER.debug("Overdue reminder with id {} moved not the next time", reminder.getId());
        }
        LOGGER.debug("Move reminders finished at {}", LocalDateTime.now());
    }

    //every minute on 10-th second
    @Scheduled(cron = "10 * * * * *")
    public void sendReminders() {
        List<Reminder> reminders = reminderService.getRemindersWithReminderTimes(LocalDateTime.now(), 30);

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
        if (reminder.isRepeatableWithTime()) {
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
                        if (reminder.isRepeatableWithTime()) {
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
        reminderNotificationService.deleteReminderNotification(reminderNotification.getId());
    }

    private void sendRepeatReminderTime(Reminder reminder, ReminderNotification reminderNotification) {
        reminderNotificationMessageSender.sendRemindMessage(reminder, reminderNotification.isItsTime());

        reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), timeCreator.localDateTimeNowWithMinutes());
    }

    private void sendRepeatReminderTimeForRepeatableReminder(Reminder reminder, ReminderNotification reminderNotification) {
        DateTime nextRemindAt = reminder.getRemindAt();

        if (repeatReminderBusinessService.isNeedUpdateNextRemindAt(reminder, reminderNotification)) {
            RemindAtCandidate nextRemindAtCandidate = repeatReminderBusinessService.getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtsInReceiverZone(timeCreator), reminder.getCurrRepeatIndex());
            nextRemindAt = nextRemindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC);
            repeatReminderBusinessService.updateNextRemindAtAndSeries(reminder.getId(), UpdateSeries.INCREMENT, nextRemindAtCandidate.getCurrentSeriesToComplete(), nextRemindAtCandidate.getIndex(), nextRemindAt);
            reminder.setRemindAt(nextRemindAt);
        }
        reminderNotificationMessageSender.sendRemindMessage(reminder, reminderNotification.isItsTime(), nextRemindAt);

        ZonedDateTime nextLastRemindAt = JodaTimeUtils.plus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
        reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), nextLastRemindAt.toLocalDateTime());
    }
}
