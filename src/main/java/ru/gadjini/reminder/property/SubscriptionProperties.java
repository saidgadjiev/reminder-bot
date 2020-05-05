package ru.gadjini.reminder.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("subscription")
public class SubscriptionProperties {

    private int trialPeriod;

    public int getTrialPeriod() {
        return trialPeriod;
    }

    public void setTrialPeriod(int trialPeriod) {
        this.trialPeriod = trialPeriod;
    }
}
