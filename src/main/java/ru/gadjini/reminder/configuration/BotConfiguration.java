package ru.gadjini.reminder.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.bot.command.keyboard.UserReminderNotificationScheduleCommand;
import ru.gadjini.reminder.common.CommandNames;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.UserReminderNotification;
import ru.gadjini.reminder.filter.BotFilter;
import ru.gadjini.reminder.filter.ReminderBotFilter;
import ru.gadjini.reminder.filter.StartCommandFilter;
import ru.gadjini.reminder.properties.WebHookProperties;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.UserReminderNotificationService;
import ru.gadjini.reminder.service.command.CommandStateService;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.message.ReminderNotificationMessageBuilder;
import ru.gadjini.reminder.service.reminder.request.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Configuration
public class BotConfiguration implements Jackson2ObjectMapperBuilderCustomizer {

    public static final String PROFILE_TEST = "test";

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Profile("!" + PROFILE_TEST)
    public TelegramBotsApi telegramBotsApi(WebHookProperties webHookProperties) throws TelegramApiRequestException {
        return new TelegramBotsApi(webHookProperties.getExternalUrl(), webHookProperties.getInternalUrl());
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public KeyboardBotCommand userReminderNotificationWithTimeCommand(CommandStateService stateService, LocalisationService localisationService,
                                                                      UserReminderNotificationService userReminderNotificationService,
                                                                      ReminderNotificationMessageBuilder messageBuilder,
                                                                      MessageService messageService,
                                                                      InlineKeyboardService inlineKeyboardService,
                                                                      CurrReplyKeyboard replyKeyboardService,
                                                                      TgUserService userService
    ) {
        Set<String> names = new HashSet<>();
        for (Locale locale : localisationService.getSupportedLocales()) {
            names.add(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME, locale));
        }

        return new UserReminderNotificationScheduleCommand(
                names,
                CommandNames.USER_REMINDER_NOTIFICATION_WITH_TIME_HISTORY_NAME,
                UserReminderNotification.NotificationType.WITH_TIME,
                userReminderNotificationService, messageBuilder,
                messageService, inlineKeyboardService, replyKeyboardService, stateService,
                localisationService, userService);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public KeyboardBotCommand userReminderNotificationWithoutTimeCommand(CommandStateService stateService, LocalisationService localisationService,
                                                                         UserReminderNotificationService userReminderNotificationService,
                                                                         ReminderNotificationMessageBuilder messageBuilder,
                                                                         MessageService messageService,
                                                                         InlineKeyboardService inlineKeyboardService,
                                                                         CurrReplyKeyboard replyKeyboardService,
                                                                         TgUserService userService
    ) {
        Set<String> names = new HashSet<>();
        for (Locale locale : localisationService.getSupportedLocales()) {
            names.add(localisationService.getMessage(MessagesProperties.USER_REMINDER_NOTIFICATION_COMMAND_NAME, locale));
        }

        return new UserReminderNotificationScheduleCommand(
                names, CommandNames.USER_REMINDER_NOTIFICATION_WITHOUT_TIME_HISTORY_NAME,
                UserReminderNotification.NotificationType.WITHOUT_TIME, userReminderNotificationService,
                messageBuilder, messageService, inlineKeyboardService, replyKeyboardService,
                stateService,
                localisationService, userService);
    }

    @Bean
    @Qualifier("chain")
    public ReminderRequestExtractor requestExtractor(MySelfRequestExtractor mySelfRequestExtractor,
                                                     ReceiverIdRequestExtractor receiverIdRequestExtractor,
                                                     WithLoginRequestExtractor withLoginRequestExtractor,
                                                     FriendRequestExtractor friendRequestExtractor) {
        friendRequestExtractor.setNext(withLoginRequestExtractor).setNext(receiverIdRequestExtractor).setNext(mySelfRequestExtractor);

        return friendRequestExtractor;
    }

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        jacksonObjectMapperBuilder.modules(new JavaTimeModule(), new JodaModule()).serializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    public BotFilter botFilter(ReminderBotFilter reminderBotFilter, StartCommandFilter startCommandFilter) {
        startCommandFilter.setNext(reminderBotFilter);

        return startCommandFilter;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DefaultBotOptions botOptions(WebHookProperties webHookProperties) {
        DefaultBotOptions defaultBotOptions = ApiContext.getInstance(DefaultBotOptions.class);

        defaultBotOptions.setMaxWebhookConnections(webHookProperties.getMaxConnections());

        return defaultBotOptions;
    }
}
