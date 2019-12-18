package ru.gadjini.reminder.configuration;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamCastMode;
import org.jooq.conf.SettingsTools;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.keyboard.UserReminderNotificationScheduleCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.properties.WebHookProperties;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;

@Configuration
public class BotConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DefaultConfiguration configuration(ConnectionProvider connectionProvider) {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider);
        jooqConfiguration.setSQLDialect(SQLDialect.POSTGRES);
        jooqConfiguration.setSettings(SettingsTools.defaultSettings().withParamCastMode(ParamCastMode.NEVER));

        return jooqConfiguration;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TelegramBotsApi telegramBotsApi(WebHookProperties webHookProperties) throws TelegramApiRequestException {
        return new TelegramBotsApi(webHookProperties.getExternalUrl(), webHookProperties.getInternalUrl());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TimeZoneEngine timeZoneEngine() {
        return TimeZoneEngine.initialize();
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
                MessagesProperties.USER_REMINDER_NOTIFICATION_WITH_TIME_HISTORY_NAME,
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
                MessagesProperties.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_HISTORY_NAME,
                UserReminderNotification.NotificationType.WITHOUT_TIME,
                userReminderNotificationService,
                messageBuilder,
                messageService,
                inlineKeyboardService,
                replyKeyboardService
        );
    }
}
