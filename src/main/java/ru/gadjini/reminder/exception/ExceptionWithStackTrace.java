package ru.gadjini.reminder.exception;

public class ExceptionWithStackTrace extends RuntimeException {

    private final StackTraceElement[] stackTraceElements;

    public ExceptionWithStackTrace(String message, StackTraceElement[] stackTraceElements) {
        super(message);
        this.stackTraceElements = stackTraceElements;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return stackTraceElements;
    }
}
