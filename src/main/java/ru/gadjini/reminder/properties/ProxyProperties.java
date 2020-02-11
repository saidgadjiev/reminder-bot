package ru.gadjini.reminder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import ru.gadjini.reminder.configuration.BotConfiguration;

@ConfigurationProperties("proxy")
@Profile(BotConfiguration.PROFILE_DEV)
public class ProxyProperties {

    private String host;

    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
