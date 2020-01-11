package ru.gadjini.reminder.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.dao.subscription.SubscriptionDao;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.properties.SubscriptionProperties;

import java.time.LocalDate;

@Service
public class SubscriptionService {

    private SubscriptionProperties subscriptionProperties;

    private SubscriptionDao subscriptionDao;

    @Autowired
    public SubscriptionService(SubscriptionProperties subscriptionProperties, @Qualifier("redis") SubscriptionDao subscriptionDao) {
        this.subscriptionProperties = subscriptionProperties;
        this.subscriptionDao = subscriptionDao;
    }

    public Subscription getSubscription(int userId) {
        return subscriptionDao.getSubscription(userId);
    }

    public void createTrialSubscription(int userId) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setEndDate(LocalDate.now().plusDays(subscriptionProperties.getTrialPeriod()));

        subscriptionDao.create(subscription);
    }

    public LocalDate renewSubscription(Plan plan, int userId) {
        return subscriptionDao.update(plan.getPeriod(), plan.getId(), userId);
    }
}
