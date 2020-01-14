package ru.gadjini.reminder.service.reminder.time;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.Reminder;

@Service
public class ReminderTimeBuilder {

    private TimeBuilder timeBuilder;

    @Autowired
    public ReminderTimeBuilder(TimeBuilder timeBuilder) {
        this.timeBuilder = timeBuilder;
    }

    public String time(Reminder reminder) {
        if (reminder.isInactive()) {
            return "<b>" + timeBuilder.deactivated() + "</b>";
        }
        if (reminder.isRepeatable()) {
            return timeBuilder.time(reminder.getRepeatRemindAtInReceiverZone());
        } else {
            return timeBuilder.time(reminder.getRemindAtInReceiverZone());
        }
    }

}
