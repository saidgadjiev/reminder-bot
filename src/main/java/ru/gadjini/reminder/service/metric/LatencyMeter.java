package ru.gadjini.reminder.service.metric;

public interface LatencyMeter {

    void start();

    void stop(String message, Object... args);
}
