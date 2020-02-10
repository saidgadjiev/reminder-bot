package ru.gadjini.reminder.service.metric;

public interface LatencyMeterLogger {

    void start();

    void stop(String message, Object... args);
}
