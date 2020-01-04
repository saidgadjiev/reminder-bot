package ru.gadjini.reminder.controller;

import org.glassfish.grizzly.threadpool.AbstractThreadPool;
import org.glassfish.grizzly.threadpool.ThreadPoolProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLoggingProbe implements ThreadPoolProbe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLoggingProbe.class);

    @Override
    public void onThreadPoolStartEvent(AbstractThreadPool threadPool) {
        LOGGER.debug("Grizzly thread pool start. Thread pool class {}. Threads count: {}", threadPool.getClass().getSimpleName(), threadPool.getConfig().getMaxPoolSize());
    }

    @Override
    public void onThreadPoolStopEvent(AbstractThreadPool threadPool) {
        LOGGER.debug("Grizzly thread pool stop.");
    }

    @Override
    public void onThreadAllocateEvent(AbstractThreadPool threadPool, Thread thread) {

    }

    @Override
    public void onThreadReleaseEvent(AbstractThreadPool threadPool, Thread thread) {

    }

    @Override
    public void onMaxNumberOfThreadsEvent(AbstractThreadPool threadPool, int maxNumberOfThreads) {

    }

    @Override
    public void onTaskQueueEvent(AbstractThreadPool threadPool, Runnable task) {

    }

    @Override
    public void onTaskDequeueEvent(AbstractThreadPool threadPool, Runnable task) {

    }

    @Override
    public void onTaskCancelEvent(AbstractThreadPool threadPool, Runnable task) {

    }

    @Override
    public void onTaskCompleteEvent(AbstractThreadPool threadPool, Runnable task) {

    }

    @Override
    public void onTaskQueueOverflowEvent(AbstractThreadPool threadPool) {

    }
}
