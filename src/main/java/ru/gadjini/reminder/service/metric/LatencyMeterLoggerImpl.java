package ru.gadjini.reminder.service.metric;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import ru.gadjini.reminder.properties.LoggingProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class LatencyMeterLoggerImpl implements LatencyMeterLogger {

    private final LoggingProperties loggingProperties;

    private final Logger logger;

    private StopWatch stopWatch = new StopWatch();

    LatencyMeterLoggerImpl(LoggingProperties loggingProperties, Logger logger) {
        this.loggingProperties = loggingProperties;
        this.logger = logger;
    }

    @Override
    public void start() {
        stopWatch.start();
    }

    @Override
    public void stop(String message, Object... args) {
        stopWatch.stop();

        if (stopWatch.getTime() > loggingProperties.getLatencyLogLevel()) {
            Collection<Object> objects = new ArrayList<>(Arrays.asList(args));

            objects.add(stopWatch.getTime());

            logger.warn(message + " latency = {}", objects.toArray());
        }
    }
}
