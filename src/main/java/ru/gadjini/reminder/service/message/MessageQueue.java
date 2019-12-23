package ru.gadjini.reminder.service.message;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MessageQueue {

    private BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();

    public void push(Runnable job) {
        blockingQueue.add(job);
    }

    public List<Runnable> poll(int count) {
        List<Runnable> poll = new ArrayList<>();

        while (count > 0) {
            Runnable runnable = blockingQueue.poll();

            if (runnable == null) {
                break;
            }
            poll.add(runnable);
            --count;
        }

        return poll;
    }
}
