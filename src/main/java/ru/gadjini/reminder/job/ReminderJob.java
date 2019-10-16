package ru.gadjini.reminder.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.ReminderTime;
import ru.gadjini.reminder.service.*;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderJob {

    private ReminderTextBuilder reminderTextBuilder;

    private ReminderService reminderService;

    private ReminderTimeService reminderTimeService;

    private MessageService messageService;

    private KeyboardService keyboardService;

    @Autowired
    public ReminderJob(ReminderTextBuilder reminderTextBuilder,
                       ReminderService reminderService,
                       ReminderTimeService reminderTimeService,
                       MessageService messageService,
                       KeyboardService keyboardService) {
        this.reminderTextBuilder = reminderTextBuilder;
        this.reminderService = reminderService;
        this.reminderTimeService = reminderTimeService;
        this.messageService = messageService;
        this.keyboardService = keyboardService;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void sendReminders() {
        List<Reminder> reminders = reminderService.getReminders(LocalDateTime.now().withSecond(0));

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
        String remindText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAt());

        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMIND, new Object[]{remindText}, keyboardService.getReminderButtons(reminder.getId()));
        reminderTimeService.deleteReminderTime(reminderTime.getId());
    }

    private void sendRepeatReminder(Reminder reminder, ReminderTime reminderTime) {
        String remindText = reminderTextBuilder.create(reminder.getText(), reminder.getRemindAt());

        messageService.sendMessageByCode(reminder.getReceiver().getChatId(), MessagesProperties.MESSAGE_REMIND, new Object[]{remindText}, keyboardService.getReminderButtons(reminder.getId()));
        reminderTimeService.updateLastRemindAt(reminderTime.getId(), LocalDateTime.now().withSecond(0));
    }
}
