package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TimeZoneDbSenderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeZoneDbSenderJob.class);

    private BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();

    @Autowired
    public TimeZoneDbSenderJob() {
        LOGGER.debug("Time zone db sender job initialized and working");
    }

    @Scheduled(fixedDelay = 1000)
    public void sendMessages() {
        try {
            Runnable job = blockingQueue.take();
            job.run();
        } catch (InterruptedException ignore) {
            LOGGER.error("Time zone db sender interrupted");
        }
    }

    public void pushJob(Runnable job) {
        blockingQueue.add(job);
    }
}
