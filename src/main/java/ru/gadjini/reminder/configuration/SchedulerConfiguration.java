package ru.gadjini.reminder.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfiguration.class);

    @Bean
    public TaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        threadPoolTaskScheduler.setErrorHandler(throwable -> LOGGER.error(throwable.getMessage(), throwable));

        LOGGER.debug("Thread pool scheduler initialized with pool size: {}", threadPoolTaskScheduler.getPoolSize());

        return threadPoolTaskScheduler;
    }
}
