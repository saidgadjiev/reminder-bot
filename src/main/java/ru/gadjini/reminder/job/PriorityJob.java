package ru.gadjini.reminder.job;

public abstract class PriorityJob implements Runnable {

    private StackTraceElement[] stackTraceElements;

    private final Priority priority;

    public PriorityJob(Priority priority) {
        this.priority = priority;
        this.stackTraceElements = Thread.currentThread().getStackTrace();
    }

    public Priority getPriority() {
        return priority;
    }

    public StackTraceElement[] getStackTraceElements() {
        return stackTraceElements;
    }

    public enum Priority {

        HIGH,

        MEDIUM,

        LOW
    }
}
