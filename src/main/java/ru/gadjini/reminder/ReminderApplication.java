package ru.gadjini.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;
import ru.gadjini.reminder.properties.BotProperties;
import ru.gadjini.reminder.properties.WebHookProperties;


@SpringBootApplication
@EnableConfigurationProperties(value = {
        BotProperties.class,
        WebHookProperties.class
})
@EnableScheduling
public class ReminderApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderApplication.class);

    public static void main(String[] args) {
        try {
            ApiContextInitializer.init();
            SpringApplication.run(ReminderApplication.class);
        } catch (Throwable ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
