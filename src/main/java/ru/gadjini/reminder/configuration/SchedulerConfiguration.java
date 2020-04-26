package ru.gadjini.reminder.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.gadjini.reminder.exception.TelegramMethodException;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.reminder.notification.ReminderNotificationService;

@Configuration
public class SchedulerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfiguration.class);

    @Bean
    public TaskScheduler jobsThreadPoolTaskScheduler(TgUserService userService) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2 + Runtime.getRuntime().availableProcessors() * 2);
        threadPoolTaskScheduler.setThreadNamePrefix("JobsThreadPoolTaskScheduler");
        threadPoolTaskScheduler.setErrorHandler(throwable -> {
            LOGGER.error(throwable.getMessage(), throwable);
            if (throwable instanceof TelegramMethodException) {
                TelegramMethodException exception = (TelegramMethodException) throwable;
                if (exception.getErrorCode() == 403) {
                    userService.blockUser((int) exception.getChatId());
                }
            }
        });

        LOGGER.debug("Jobs thread pool scheduler initialized with pool size: {}", threadPoolTaskScheduler.getPoolSize());

        return threadPoolTaskScheduler;
    }

    @Bean
    public TaskExecutor reminderExecutorService() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        taskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        taskExecutor.setThreadNamePrefix("ReminderJobThread");
        taskExecutor.initialize();

        LOGGER.debug("Reminder thread pool executor initialized with pool size: {}", taskExecutor.getCorePoolSize());

        return taskExecutor;
    }
}
