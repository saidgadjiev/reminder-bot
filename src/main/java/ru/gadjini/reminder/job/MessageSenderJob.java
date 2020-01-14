package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.configuration.BotConfiguration;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

@Component
@Profile("!" + BotConfiguration.PROFILE_TEST)
public class MessageSenderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSenderJob.class);

    private BlockingQueue<PriorityJob> jobsQueue = new PriorityBlockingQueue<>(40, Comparator.comparing(PriorityJob::getPriority));

    public MessageSenderJob() {
        LOGGER.debug("Message sender job initialized and working");
    }

    @Scheduled(fixedDelay = 20)
    public void send() {
        try {
            PriorityJob job = jobsQueue.take();

            job.run();
        } catch (InterruptedException e) {
            LOGGER.error("Message sender job interrupted");
        }
    }

    public void push(PriorityJob priorityJob) {
        jobsQueue.add(priorityJob);
    }
}
