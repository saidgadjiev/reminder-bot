package ru.gadjini.reminder.job;


public abstract class PriorityJob implements Runnable {

    private StackTraceElement[] stackTraceElements;

    private final Priority priority;

    private long queuedAt;

    public PriorityJob(Priority priority) {
        this.priority = priority;
        this.stackTraceElements = Thread.currentThread().getStackTrace();
        queuedAt = System.currentTimeMillis();
    }

    public Priority getPriority() {
        return priority;
    }

    public StackTraceElement[] getStackTraceElements() {
        return stackTraceElements;
    }

    public long getQueuedAt() {
        return queuedAt;
    }

    public enum Priority {

        HIGH,

        MEDIUM,

        LOW
    }
}
