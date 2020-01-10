package ru.gadjini.reminder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public class AppProperties {

    private String url;

    private String telegramRedirectUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTelegramRedirectUrl() {
        return telegramRedirectUrl;
    }

    public void setTelegramRedirectUrl(String telegramRedirectUrl) {
        this.telegramRedirectUrl = telegramRedirectUrl;
    }
}
