package ru.gadjini.reminder.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("webhook")
public class WebHookProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebHookProperties.class);

    private String externalUrl;

    private String internalUrl;

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
        LOGGER.info("WebHook external url: " + externalUrl);
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }
}
