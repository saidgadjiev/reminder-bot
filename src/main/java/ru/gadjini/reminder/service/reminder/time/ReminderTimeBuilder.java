package ru.gadjini.reminder.service.reminder.time;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.util.TimeCreator;

import java.util.Locale;

@Service
public class ReminderTimeBuilder {

    private TimeBuilder timeBuilder;

    private TimeCreator timeCreator;

    @Autowired
    public ReminderTimeBuilder(TimeBuilder timeBuilder, TimeCreator timeCreator) {
        this.timeBuilder = timeBuilder;
        this.timeCreator = timeCreator;
    }

    public String time(Reminder reminder, Locale locale) {
        if (reminder.isInactive()) {
            return "<b>" + timeBuilder.deactivated(locale) + "</b>";
        }
        if (reminder.isRepeatable() || reminder.isRepeatableWithoutTime()) {
            return timeBuilder.time(reminder.getRepeatRemindAtsInReceiverZone(timeCreator), locale);
        } else {
            return timeBuilder.time(reminder.getRemindAtInReceiverZone(), locale);
        }
    }

}
