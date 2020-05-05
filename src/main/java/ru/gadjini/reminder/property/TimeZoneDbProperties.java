package ru.gadjini.reminder.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tz")
public class TimeZoneDbProperties {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
