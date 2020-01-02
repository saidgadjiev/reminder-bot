package ru.gadjini.reminder.configuration;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.keyboard.UserReminderNotificationScheduleCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.properties.WebHookProperties;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;
import ru.gadjini.reminder.service.reminder.request.*;

@Configuration
public class BotConfiguration {

    public static final String PROFILE_PROD = "prod";

    public static final String PROFILE_TEST = "test";

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Profile("!" + PROFILE_TEST)
    public TelegramBotsApi telegramBotsApi(WebHookProperties webHookProperties) throws TelegramApiRequestException {
        return new TelegramBotsApi(webHookProperties.getExternalUrl(), webHookProperties.getInternalUrl());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public KeyboardBotCommand userReminderNotificationWithTimeCommand(LocalisationService localisationService,
                                                                      UserReminderNotificationService userReminderNotificationService,
                                                                      ReminderNotificationMessageBuilder messageBuilder,
                                                                      MessageService messageService,
                                                                      InlineKeyboardService inlineKeyboardService,
                                                                      ReplyKeyboardService replyKeyboardService
    ) {
        return new UserReminderNotificationScheduleCommand(
                localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITH_TIME_COMMAND_NAME),
                CommandNames.USER_REMINDER_NOTIFICATION_WITH_TIME_HISTORY_NAME,
                UserReminderNotification.NotificationType.WITH_TIME,
                userReminderNotificationService,
                messageBuilder,
                messageService,
                inlineKeyboardService,
                replyKeyboardService
        );
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public KeyboardBotCommand userReminderNotificationWithoutTimeCommand(LocalisationService localisationService,
                                                                         UserReminderNotificationService userReminderNotificationService,
                                                                         ReminderNotificationMessageBuilder messageBuilder,
                                                                         MessageService messageService,
                                                                         InlineKeyboardService inlineKeyboardService,
                                                                         ReplyKeyboardService replyKeyboardService
    ) {
        return new UserReminderNotificationScheduleCommand(
                localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_COMMAND_NAME),
                CommandNames.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_HISTORY_NAME,
                UserReminderNotification.NotificationType.WITHOUT_TIME,
                userReminderNotificationService,
                messageBuilder,
                messageService,
                inlineKeyboardService,
                replyKeyboardService
        );
    }

    @Bean
    public ReminderRequestExtractor requestExtractor(MySelfRequestExtractor mySelfRequestExtractor,
                                                     ReceiverIdRequestExtractor receiverIdRequestExtractor,
                                                     WithLoginRequestExtractor withLoginRequestExtractor,
                                                     FriendRequestExtractor friendRequestExtractor) {
        receiverIdRequestExtractor.setNext(withLoginRequestExtractor).setNext(friendRequestExtractor).setNext(mySelfRequestExtractor);

        return receiverIdRequestExtractor;
    }
}
