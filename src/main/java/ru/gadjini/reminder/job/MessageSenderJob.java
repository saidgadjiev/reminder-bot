package ru.gadjini.reminder.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.configuration.BotConfiguration;
import ru.gadjini.reminder.service.message.MessageQueue;

@Component
@Profile("!" + BotConfiguration.PROFILE_TEST)
public class MessageSenderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSenderJob.class);

    private MessageQueue messageQueue;

    @Autowired
    public MessageSenderJob(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;

        LOGGER.debug("Message sender job initialized and working");
    }

    @Scheduled(fixedDelay = 35)
    public void sendMessages() throws Exception {
        Runnable job = messageQueue.take();
        job.run();
    }
}
