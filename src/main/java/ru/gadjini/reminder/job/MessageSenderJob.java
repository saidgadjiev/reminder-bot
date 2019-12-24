package ru.gadjini.reminder.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.service.message.MessageQueue;

import java.util.List;

@Component
public class MessageSenderJob {

    private static final int BATCH_SIZE = 1;

    private MessageQueue messageQueue;

    @Autowired
    public MessageSenderJob(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Scheduled(fixedDelay = 40)
    public void sendMessages() throws Exception {
        for (int i = 0; i < BATCH_SIZE; ++i) {
            Runnable job = messageQueue.take();
            job.run();
        }
    }
}
