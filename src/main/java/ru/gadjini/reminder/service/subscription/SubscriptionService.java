package ru.gadjini.reminder.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.subscription.SubscriptionDao;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.property.SubscriptionProperties;
import ru.gadjini.reminder.util.DateTimeService;

import java.time.LocalDate;

@Service
public class SubscriptionService {

    private SubscriptionProperties subscriptionProperties;

    private SubscriptionDao subscriptionDao;

    private DateTimeService timeCreator;

    @Autowired
    public SubscriptionService(SubscriptionProperties subscriptionProperties, @Qualifier("redis") SubscriptionDao subscriptionDao, DateTimeService timeCreator) {
        this.subscriptionProperties = subscriptionProperties;
        this.subscriptionDao = subscriptionDao;
        this.timeCreator = timeCreator;
    }

    public Subscription getSubscription(long userId) {
        return subscriptionDao.getSubscription(userId);
    }

    public void createTrialSubscription(long userId) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setEndDate(timeCreator.localDateNow().plusDays(subscriptionProperties.getTrialPeriod()));

        subscriptionDao.create(subscription);
    }

    public LocalDate renewSubscription(Plan plan, long userId) {
        return subscriptionDao.update(plan.getPeriod(), plan.getId(), userId);
    }
}
