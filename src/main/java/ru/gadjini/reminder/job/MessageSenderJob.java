package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.service.metric.LoggingSystem;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

@Component
@Profile("!" + BotConfiguration.PROFILE_TEST)
public class MessageSenderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSenderJob.class);

    private BlockingQueue<PriorityJob> jobsQueue = new PriorityBlockingQueue<>(100, Comparator.comparing(PriorityJob::getPriority));

    private LoggingSystem loggingSystem;

    @Autowired
    public MessageSenderJob(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;

        LOGGER.debug("Message sender job initialized and working");
    }

    @Scheduled(fixedDelay = 20)
    public void send() {
        try {
            PriorityJob job = jobsQueue.take();

            loggingSystem.logPriorityJob(job);
            try {
                job.run();
            } catch (Exception e) {
                e.setStackTrace(job.getStackTraceElements());
                throw e;
            }
        } catch (InterruptedException e) {
            LOGGER.error("Message sender job interrupted");
        }
    }

    public void push(PriorityJob priorityJob) {
        jobsQueue.add(priorityJob);
    }
}
