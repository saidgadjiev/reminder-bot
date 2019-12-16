package ru.gadjini.reminder.service;

import org.springframework.stereotype.Service;
import ru.gadjini.reminder.domain.UserReminderNotification;

import java.time.LocalTime;
import java.util.List;

@Service
public class UserReminderNotificationService {

    public List<UserReminderNotification> getUserReminderNotifications(int userId, UserReminderNotification.ReminderType reminderType) {
        if (reminderType.equals(UserReminderNotification.ReminderType.WITH_TIME)) {
            return List.of(new UserReminderNotification() {{
                setOffsetTime(new OffsetTime(1, LocalTime.of(22, 0)));
                setOffsetTime(new OffsetTime(2, 0));
                setOffsetTime(new OffsetTime(1, 0));
                setOffsetTime(new OffsetTime(0, 20));
            }});
        } else {
            return List.of(new UserReminderNotification() {{
                setOffsetTime(new OffsetTime(1, LocalTime.of(22, 0)));
                setOffsetTime(new OffsetTime(0, LocalTime.of(12, 0)));
                setOffsetTime(new OffsetTime(0, LocalTime.of(22, 0)));
            }});
        }
    }
}
