package ru.gadjini.reminder.service.message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//@Component
public class MessageQueue {

    private BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();

    public void push(Runnable job) {
        blockingQueue.add(job);
    }

    public Runnable take() throws InterruptedException {
        return blockingQueue.take();
    }
}
