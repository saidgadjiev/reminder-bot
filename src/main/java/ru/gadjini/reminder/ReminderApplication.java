package ru.gadjini.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.gadjini.reminder.bot.api.ApiContextInitializer;
import ru.gadjini.reminder.properties.BotProperties;
import ru.gadjini.reminder.properties.SubscriptionProperties;
import ru.gadjini.reminder.properties.TimeZoneDbProperties;
import ru.gadjini.reminder.properties.WebHookProperties;


@SpringBootApplication
@EnableConfigurationProperties(value = {
        BotProperties.class,
        WebHookProperties.class,
        TimeZoneDbProperties.class,
        SubscriptionProperties.class
})
@EnableScheduling
public class ReminderApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderApplication.class);

    public static void main(String[] args) {
        ApiContextInitializer.init();
        try {
            SpringApplication.run(ReminderApplication.class);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
