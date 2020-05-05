package ru.gadjini.reminder.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("logging")
public class LoggingProperties {

    private Long latencyLogLevel;

    private Long messageTimeLogLevel;

    public Long getLatencyLogLevel() {
        return latencyLogLevel;
    }

    public void setLatencyLogLevel(Long latencyLogLevel) {
        this.latencyLogLevel = latencyLogLevel;
    }

    public Long getMessageTimeLogLevel() {
        return messageTimeLogLevel;
    }

    public void setMessageTimeLogLevel(Long messageTimeLogLevel) {
        this.messageTimeLogLevel = messageTimeLogLevel;
    }
}
