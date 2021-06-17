package ru.gadjini.reminder.dao.subscription;

import org.joda.time.Period;
import ru.gadjini.reminder.domain.Subscription;

import java.time.LocalDate;

public interface SubscriptionDao {
    Subscription getSubscription(long userId);

    void create(Subscription subscription);

    LocalDate update(Period period, int planId, long userId);
}
