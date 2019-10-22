package ru.gadjini.reminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;
import ru.gadjini.reminder.properties.BotProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {
        BotProperties.class
})
@EnableScheduling
public class ReminderApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(ReminderApplication.class);
    }
}
