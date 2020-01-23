package ru.gadjini.reminder.service.reminder.time;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.util.TimeCreator;

@Service
public class ReminderTimeBuilder {

    private TimeBuilder timeBuilder;

    private TimeCreator timeCreator;

    @Autowired
    public ReminderTimeBuilder(TimeBuilder timeBuilder, TimeCreator timeCreator) {
        this.timeBuilder = timeBuilder;
        this.timeCreator = timeCreator;
    }

    public String time(Reminder reminder) {
        if (reminder.isInactive()) {
            return "<b>" + timeBuilder.deactivated() + "</b>";
        }
        if (reminder.isRepeatable()) {
            return timeBuilder.time(reminder.getRepeatRemindAtInReceiverZone(timeCreator));
        } else {
            return timeBuilder.time(reminder.getRemindAtInReceiverZone());
        }
    }

}
