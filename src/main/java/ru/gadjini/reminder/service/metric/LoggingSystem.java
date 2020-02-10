package ru.gadjini.reminder.service.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.properties.LoggingProperties;
import ru.gadjini.reminder.properties.WebHookProperties;

@Service
public class LoggingSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingSystem.class);

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final LoggingProperties loggingProperties;

    @Autowired
    public LoggingSystem(WebHookProperties webHookProperties, LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
        LOGGER.info("Web hook external url: " + webHookProperties.getExternalUrl());
        LOGGER.info("Web hook max connections: " + webHookProperties.getMaxConnections());
    }

    public LatencyMeterLogger getLatencyMeterLogger() {
        if (loggingProperties.getLatencyLogLevel() == null) {
            return new DummyLatencyMeterLogger();
        }

        return activeProfile.equals(BotConfiguration.PROFILE_TEST) ? new DummyLatencyMeterLogger() : new LatencyMeterLoggerImpl(loggingProperties, LOGGER);
    }

    public void logPriorityJob(PriorityJob priorityJob) {
        if (loggingProperties.getMessageTimeLogLevel() != null) {
            long queuedAt = priorityJob.getQueuedAt();
            long time = System.currentTimeMillis() - queuedAt;

            if (time > loggingProperties.getMessageTimeLogLevel()) {
                LOGGER.warn("Priority job({}) latency: {}", priorityJob.getPriority(), time);
            }
        }
    }
}
