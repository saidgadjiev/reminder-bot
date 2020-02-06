package ru.gadjini.reminder.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.common.ReminderConstants;
import ru.gadjini.reminder.domain.CreateOrUpdateResult;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.command.CommandNavigator;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;

import java.time.ZoneId;
import java.time.format.TextStyle;

@Component
public class StartCommandFilter extends BaseBotFilter {

    private CommandParser commandParser;

    private UserReminderNotificationService userReminderNotificationService;

    private TgUserService tgUserService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private CurrReplyKeyboard currReplyKeyboard;

    private CommandNavigator commandNavigator;

    @Autowired
    public StartCommandFilter(CommandParser commandParser,
                              UserReminderNotificationService userReminderNotificationService,
                              TgUserService tgUserService, MessageService messageService,
                              LocalisationService localisationService, CurrReplyKeyboard currReplyKeyboard, CommandNavigator commandNavigator) {
        this.commandParser = commandParser;
        this.userReminderNotificationService = userReminderNotificationService;
        this.tgUserService = tgUserService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.currReplyKeyboard = currReplyKeyboard;
        this.commandNavigator = commandNavigator;
    }

    @Override
    public void doFilter(Update update) {
        if (isStartCommand(update)) {
            boolean isNew = doStart(update);

            if (isNew) {
                return;
            }
        }

        super.doFilter(update);
    }

    private boolean doStart(Update update) {
        CreateOrUpdateResult createOrUpdateResult = tgUserService.createOrUpdateUser(update.getMessage().getChatId(), update.getMessage().getFrom());

        if (createOrUpdateResult.isCreated()) {
            createUserNotifications(update.getMessage().getFrom().getId());

            messageService.sendMessageAsync(
                    new SendMessageContext(PriorityJob.Priority.HIGH)
                            .chatId(update.getMessage().getChatId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_START, new Object[]{
                                    createOrUpdateResult.getUser().getZone().getDisplayName(TextStyle.FULL, createOrUpdateResult.getUser().getLocale())
                            }, createOrUpdateResult.getUser().getLocale()))
                            .replyKeyboard(currReplyKeyboard.getMainMenu(update.getMessage().getChatId(), createOrUpdateResult.getUser().getLocale()))
            );

            commandNavigator.setCurrentCommand(update.getMessage().getChatId(), CommandNames.START_COMMAND_NAME);

            return true;
        }

        return false;
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
