package ru.gadjini.reminder.service.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.service.ReminderBotService;

@Component
public class ReminderBotFilter extends BaseBotFilter {

    private ReminderBotService reminderBotService;

    @Autowired
    public ReminderBotFilter(ReminderBotService reminderBotService) {
        this.reminderBotService = reminderBotService;
    }

    @Override
    public void doFilter(Update update) {
        reminderBotService.handleUpdate(update);
    }
}
