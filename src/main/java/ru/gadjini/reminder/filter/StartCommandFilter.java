package ru.gadjini.reminder.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.command.CommandParser;

@Component
public class StartCommandFilter extends BaseBotFilter {

    private CommandParser commandParser;

    private UserReminderNotificationService userReminderNotificationService;

    private TgUserService tgUserService;

    @Autowired
    public StartCommandFilter(CommandParser commandParser,
                              UserReminderNotificationService userReminderNotificationService,
                              TgUserService tgUserService) {
        this.commandParser = commandParser;
        this.userReminderNotificationService = userReminderNotificationService;
        this.tgUserService = tgUserService;
    }

    @Override
    public void doFilter(Update update) {
        if (isStartCommand(update)) {
            tgUserService.createOrUpdateUser(update.getMessage().getChatId(), update.getMessage().getFrom());
            createUserNotifications(update.getMessage().getFrom().getId());
        }

        super.doFilter(update);
    }

    private boolean isStartCommand(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            String commandName = commandParser.parseBotCommandName(update.getMessage());

            return commandName.equals(CommandNames.START_COMMAND_NAME);
        }

        return false;
    }

    private void createUserNotifications(int userId) {
        int countWithTime = userReminderNotificationService.count(userId, UserReminderNotification.NotificationType.WITH_TIME);
        if (countWithTime == 0) {
            userReminderNotificationService.createDefaultNotificationsForWithTime(userId);
        }

        int countWithoutTime = userReminderNotificationService.count(userId, UserReminderNotification.NotificationType.WITHOUT_TIME);
        if (countWithoutTime == 0) {
            userReminderNotificationService.createDefaultNotificationsForWithoutTime(userId);
        }
    }
}
