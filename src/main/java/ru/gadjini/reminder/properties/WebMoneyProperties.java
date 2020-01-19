package ru.gadjini.reminder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("web-money")
public class WebMoneyProperties {

    private String purse;

    private String secretKey;

    public String getPurse() {
        return purse;
    }

    public void setPurse(String purse) {
        this.purse = purse;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
