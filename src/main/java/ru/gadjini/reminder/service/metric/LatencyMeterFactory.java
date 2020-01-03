package ru.gadjini.reminder.service.metric;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.configuration.BotConfiguration;

@Service
public class LatencyMeterFactory {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public LatencyMeter getMeter() {
        return activeProfile.equals(BotConfiguration.PROFILE_TEST) ? new DummyLatencyMeter() : new LoggingLatencyMeter();
    }
}
