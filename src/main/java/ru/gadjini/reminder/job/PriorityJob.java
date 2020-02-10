package ru.gadjini.reminder.job;

import org.apache.commons.lang3.time.StopWatch;

public abstract class PriorityJob implements Runnable {

    private StackTraceElement[] stackTraceElements;

    private final Priority priority;

    private StopWatch queueWatch = new StopWatch();

    public PriorityJob(Priority priority) {
        this.priority = priority;
        this.stackTraceElements = Thread.currentThread().getStackTrace();
        queueWatch.start();
    }

    public Priority getPriority() {
        return priority;
    }

    public StackTraceElement[] getStackTraceElements() {
        return stackTraceElements;
    }

    public StopWatch getQueueWatch() {
        return queueWatch;
    }

    public enum Priority {

        HIGH,

        MEDIUM,

        LOW
    }
}
