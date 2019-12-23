package ru.gadjini.reminder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import ru.gadjini.reminder.configuration.BotConfiguration;

@Profile("!" + BotConfiguration.PROFILE_TEST)
@ConfigurationProperties("web-hook")
public class WebHookProperties {

    private String externalUrl;

    private String internalUrl;

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }
}
