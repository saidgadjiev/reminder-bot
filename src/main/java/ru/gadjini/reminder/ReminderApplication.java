package ru.gadjini.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.gadjini.reminder.property.*;

import java.util.Locale;


@SpringBootApplication
@EnableConfigurationProperties(value = {
        BotProperties.class,
        TimeZoneDbProperties.class,
        SubscriptionProperties.class,
        LoggingProperties.class,
        ProxyProperties.class
})
@EnableScheduling
public class ReminderApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderApplication.class);

    public static void main(String[] args) {
        startLogs();
        try {
            SpringApplication application = new SpringApplication(ReminderApplication.class);
            application.setApplicationContextClass(AnnotationConfigApplicationContext.class);

            application.run();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    private static void startLogs() {
        LOGGER.info("Server timezone: " + System.getProperty("user.timezone"));
        LOGGER.info("Server default locale: " + Locale.getDefault().getLanguage());
    }
}
