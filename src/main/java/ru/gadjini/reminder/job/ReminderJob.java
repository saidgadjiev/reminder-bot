package ru.gadjini.reminder.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.service.KeyboardService;
import ru.gadjini.reminder.service.MessageService;
import ru.gadjini.reminder.service.ReminderService;
import ru.gadjini.reminder.service.ReminderTimeService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReminderJob {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ReminderService reminderService;

    private ReminderTimeService reminderTimeService;

    private MessageService messageService;

    private KeyboardService keyboardService;

    @Autowired
    public ReminderJob(ReminderService reminderService, ReminderTimeService reminderTimeService, MessageService messageService, KeyboardService keyboardService) {
        this.reminderService = reminderService;
        this.reminderTimeService = reminderTimeService;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
    }

    @Scheduled(cron = "* * * * *")
    public void sendReminders() {
        List<Reminder> reminders = reminderService.getReminders(LocalDateTime.now());

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
        String remindText = reminder.getText() + " " + DATE_TIME_FORMATTER.format(reminder.getRemindAt());

        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMIND, new Object[]{remindText}, keyboardService.getReminderButtons(reminder.getId()));
        reminderTimeService.deleteReminderTime(reminderTime.getId());
    }

    private void sendRepeatReminder(Reminder reminder, ReminderTime reminderTime) {
        String remindText = reminder.getText() + " " + DATE_TIME_FORMATTER.format(reminder.getRemindAt());

        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMIND, new Object[]{remindText}, keyboardService.getReminderButtons(reminder.getId()));
        reminderTimeService.updateLastRemindAt(reminderTime.getId(), LocalDateTime.now());
    }
}
