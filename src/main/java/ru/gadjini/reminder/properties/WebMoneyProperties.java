package ru.gadjini.reminder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@ConfigurationProperties("webmoney")
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

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(Base64.getEncoder().encodeToString("Тест".getBytes(StandardCharsets.UTF_8)));
    }
}
