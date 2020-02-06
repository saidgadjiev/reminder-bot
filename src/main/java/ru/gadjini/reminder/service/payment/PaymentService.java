package ru.gadjini.reminder.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.PaymentType;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.exception.UserException;
import ru.gadjini.reminder.model.WebMoneyPayment;
import ru.gadjini.reminder.properties.WebMoneyProperties;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.subscription.PaymentMessageService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    private PlanService planService;

    private SubscriptionService subscriptionService;

    private LocalisationService localisationService;

    private WebMoneyProperties webMoneyProperties;

    private PaymentMessageService paymentMessageService;

    @Autowired
    public PaymentService(PlanService planService, SubscriptionService subscriptionService,
                          LocalisationService localisationService, WebMoneyProperties webMoneyProperties,
                          PaymentMessageService paymentMessageService) {
        this.planService = planService;
        this.subscriptionService = subscriptionService;
        this.localisationService = localisationService;
        this.webMoneyProperties = webMoneyProperties;
        this.paymentMessageService = paymentMessageService;
    }

    public PaymentProcessResult processPayment(WebMoneyPayment webMoneyPayment) {
        Plan plan = planService.getPlan(webMoneyPayment.planId());
        validate(plan, webMoneyPayment.locale());
        validate(plan, webMoneyPayment);

        LocalDate localDate = subscriptionService.renewSubscription(plan, webMoneyPayment.userId());
        Integer paymentMessageId = null;
        try {
            paymentMessageId = paymentMessageService.delete(webMoneyPayment.userId());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return new PaymentProcessResult(localDate, paymentMessageId);
    }

    public Map<String, Object> processPaymentRequest(int planId, int userId, PaymentType paymentType, Locale locale) {
        Plan plan = planService.getPlan(planId);
        validate(plan, locale);

        return Map.of(
                "planId", planId,
                "userId", userId,
                "paymentType", paymentType.getType(),
                "price", plan.getPrice(),
                "paymentDescription", Base64.getEncoder().encodeToString(plan.getPaymentDescription().getBytes(StandardCharsets.UTF_8)),
                "payeePurse", webMoneyProperties.getPurse());
    }

    private void validate(Plan plan, Locale locale) {
        if (!plan.isActive()) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_INVALID_PLAN, locale));
        }
    }

    private void validate(Plan plan, WebMoneyPayment webMoneyPayment) {
        if (!webMoneyProperties.getSecretKey().equals(webMoneyPayment.secretKey())) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_BAD_PAYMENT_REQUEST, webMoneyPayment.locale()));
        }
        validate(plan, webMoneyPayment.paymentAmount(), webMoneyPayment.locale());
        if (!webMoneyProperties.getPurse().equals(webMoneyPayment.payeePurse())) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_BAD_PAYMENT_REQUEST, webMoneyPayment.locale()));
        }
    }

    private void validate(Plan plan, double paymentAmount, Locale locale) {
        if (plan.getPrice() > paymentAmount) {
            throw new UserException(localisationService.getCurrentLocaleMessage(MessagesProperties.MESSAGE_BAD_PAYMENT_REQUEST, locale));
        }
    }

    public static class PaymentProcessResult {

        private LocalDate subscriptionEnd;

        private Integer paymentMessageId;

        private PaymentProcessResult(LocalDate subscriptionEnd, Integer paymentMessageId) {
            this.subscriptionEnd = subscriptionEnd;
            this.paymentMessageId = paymentMessageId;
        }

        public LocalDate getSubscriptionEnd() {
            return subscriptionEnd;
        }

        public Integer getPaymentMessageId() {
            return paymentMessageId;
        }
    }
}
