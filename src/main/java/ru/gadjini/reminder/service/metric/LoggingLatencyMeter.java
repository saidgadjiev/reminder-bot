package ru.gadjini.reminder.service.metric;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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

        Collection<Object> objects = new ArrayList<>(Arrays.asList(args));
        objects.add(stopWatch.getTime());

        LOGGER.debug(message + " latency = {}", objects.toArray());
    }
}
