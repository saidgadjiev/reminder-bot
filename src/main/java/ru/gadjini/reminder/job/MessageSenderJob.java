package ru.gadjini.reminder.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.reminder.service.message.MessageQueue;

import java.util.List;

@Component
public class MessageSenderJob {

    private MessageQueue messageQueue;

    @Autowired
    public MessageSenderJob(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Scheduled(fixedDelay = 1000)
    public void sendMessages() {
        List<Runnable> poll = messageQueue.poll(25);

        for (Runnable job: poll) {
            job.run();
        }
    }
}
