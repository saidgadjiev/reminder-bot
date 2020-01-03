package ru.gadjini.reminder.service.metric;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingLatencyMeter implements LatencyMeter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingLatencyMeter.class);

    private StopWatch stopWatch = new StopWatch();

    @Override
    public void start() {
        stopWatch.start();
    }

    @Override
    public void stop(String message, Object... args) {
        stopWatch.stop();
        LOGGER.debug(message + " latency = {}", args, stopWatch.getTime());
    }
}
