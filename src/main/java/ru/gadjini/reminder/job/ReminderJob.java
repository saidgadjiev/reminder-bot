package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderNotification;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.ReminderService;
import ru.gadjini.reminder.service.reminder.RepeatReminderService;
import ru.gadjini.reminder.service.reminder.RestoreReminderService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageSender;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;
import ru.gadjini.reminder.time.DateTime;
import ru.gadjini.reminder.util.JodaTimeUtils;
import ru.gadjini.reminder.util.TimeCreator;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@Profile("!" + BotConfiguration.PROFILE_TEST)
public class ReminderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderJob.class);

    private ReminderService reminderService;

    private ReminderNotificationService reminderNotificationService;

    private ReminderNotificationMessageSender reminderNotificationMessageSender;

    private RepeatReminderService repeatReminderService;

    private RestoreReminderService restoreReminderService;

    private MessageService messageService;

    private TimeCreator timeCreator;

    @Autowired
    public ReminderJob(ReminderService reminderService,
                       ReminderNotificationService reminderNotificationService,
                       ReminderNotificationMessageSender reminderNotificationMessageSender,
                       RepeatReminderService repeatReminderService,
                       RestoreReminderService restoreReminderService,
                       MessageService messageService, TimeCreator timeCreator) {
        this.reminderService = reminderService;
        this.reminderNotificationService = reminderNotificationService;
        this.reminderNotificationMessageSender = reminderNotificationMessageSender;
        this.repeatReminderService = repeatReminderService;
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

    //00:00
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteCompletedReminders() {
        int deleted = reminderService.deleteCompletedReminders(timeCreator.localDateTimeNow());

        LOGGER.debug("Delete {} completed reminders at {}", deleted, LocalDateTime.now());
    }

    //every hour
    @Scheduled(cron = "0 0 * * * *")
    public void moveReminders() {
        List<Reminder> overdueReminders = repeatReminderService.getOverdueRepeatReminders();

        for (Reminder reminder : overdueReminders) {
            repeatReminderService.autoSkip(reminder);
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

        reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), timeCreator.localDateTimeNow());
    }

    private void sendRepeatReminderTimeForRepeatableReminder(Reminder reminder, ReminderNotification reminderNotification) {
        DateTime nextRemindAt = reminder.getRemindAt();

        if (repeatReminderService.isNeedUpdateNextRemindAt(reminder, reminderNotification)) {
            RepeatReminderService.RemindAtCandidate nextRemindAtCandidate = repeatReminderService.getNextRemindAt(reminder.getRemindAtInReceiverZone(), reminder.getRepeatRemindAtsInReceiverZone(timeCreator), reminder.getCurrRepeatIndex());
            nextRemindAt = nextRemindAtCandidate.getRemindAt().withZoneSameInstant(ZoneOffset.UTC);
            repeatReminderService.updateNextRemindAtAndSeries(reminder.getId(), RepeatReminderService.UpdateSeries.INCREMENT, nextRemindAtCandidate.getCurrentSeriesToComplete(), nextRemindAtCandidate.getIndex(), nextRemindAt);
            reminder.setRemindAt(nextRemindAt);
        }
        reminderNotificationMessageSender.sendRemindMessage(reminder, reminderNotification.isItsTime(), nextRemindAt);

        ZonedDateTime nextLastRemindAt = JodaTimeUtils.plus(reminderNotification.getLastReminderAt(), reminderNotification.getDelayTime());
        reminderNotificationService.updateLastRemindAt(reminderNotification.getId(), nextLastRemindAt.toLocalDateTime());
    }
}
