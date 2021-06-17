package ru.gadjini.reminder.service.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.property.LoggingProperties;

@Service
public class LoggingSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingSystem.class);

    private final LoggingProperties loggingProperties;

    @Autowired
    public LoggingSystem(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public LatencyMeterLogger getLatencyMeterLogger() {
        if (loggingProperties.getLatencyLogLevel() == null) {
            return new DummyLatencyMeterLogger();
        }

        return new LatencyMeterLoggerImpl(loggingProperties, LOGGER);
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
