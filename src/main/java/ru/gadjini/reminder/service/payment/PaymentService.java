package ru.gadjini.reminder.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;

import java.time.LocalDate;

@Service
public class PaymentService {

    private PlanService planService;

    private SubscriptionService subscriptionService;

    private LocalisationService localisationService;

    @Autowired
    public PaymentService(PlanService planService, SubscriptionService subscriptionService, LocalisationService localisationService) {
        this.planService = planService;
        this.subscriptionService = subscriptionService;
        this.localisationService = localisationService;
    }

    public LocalDate processPayment(int userId, int planId) {
        Plan plan = planService.getPlan(planId);
        validate(plan);
        return subscriptionService.renewSubscription(plan, userId);
    }

    public void validate(Plan plan) {
        if (!plan.isActive()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_INVALID_PLAN));
        }
    }
}
