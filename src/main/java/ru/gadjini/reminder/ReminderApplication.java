package ru.gadjini.reminder;

import org.joda.time.Period;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamCastMode;
import org.jooq.conf.SettingsTools;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.gadjini.reminder.bot.api.ApiContextInitializer;
import ru.gadjini.reminder.domain.Reminder;
import ru.gadjini.reminder.domain.jooq.ReminderTable;
import ru.gadjini.reminder.domain.jooq.datatype.RepeatTimeRecord;
import ru.gadjini.reminder.domain.time.RepeatTime;
import ru.gadjini.reminder.properties.*;

import java.time.ZoneOffset;
import java.util.Map;


@SpringBootApplication
@EnableConfigurationProperties(value = {
        BotProperties.class,
        WebHookProperties.class,
        TimeZoneDbProperties.class,
        SubscriptionProperties.class,
        WebHookProperties.class,
        WebMoneyProperties.class
})
@EnableScheduling
public class ReminderApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderApplication.class);

    public static void main(String[] args) {
        ApiContextInitializer.init();
        try {
            SpringApplication application = new SpringApplication(ReminderApplication.class);
            application.setApplicationContextClass(AnnotationConfigApplicationContext.class);

            application.run();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
