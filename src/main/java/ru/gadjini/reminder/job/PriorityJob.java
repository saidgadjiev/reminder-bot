package ru.gadjini.reminder.job;

public abstract class PriorityJob implements Runnable {

    private final Priority priority;

    public PriorityJob(Priority priority) {
        this.priority = priority;
    }

    public Priority getPriority() {
        return priority;
    }

    public enum Priority {

        HIGH,

        MEDIUM,

        LOW
    }
}
