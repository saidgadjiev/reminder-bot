package ru.gadjini.reminder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import org.telegram.telegrambots.ApiContextInitializer;
import ru.gadjini.reminder.model.ReminderRequest;
import ru.gadjini.reminder.properties.BotProperties;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableConfigurationProperties(value = {
        BotProperties.class
})
@EnableScheduling
public class ReminderApplication implements CommandLineRunner {

    @Autowired
    private SmartValidator validator;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(ReminderApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        ReminderRequest reminderRequest = new ReminderRequest();

        reminderRequest.setRemindAt(LocalDateTime.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(reminderRequest, "reminderRequest");

        validator.validate(reminderRequest, bindingResult);

        System.out.println("YES");
    }
}
