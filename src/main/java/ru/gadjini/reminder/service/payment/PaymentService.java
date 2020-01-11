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
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.subscription.PaymentMessageService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    private PlanService planService;

    private SubscriptionService subscriptionService;

    private LocalisationService localisationService;

    private WebMoneyProperties webMoneyProperties;

    private TgUserService userService;

    private PaymentMessageService paymentMessageService;

    @Autowired
    public PaymentService(PlanService planService, SubscriptionService subscriptionService,
                          LocalisationService localisationService, WebMoneyProperties webMoneyProperties,
                          TgUserService userService, PaymentMessageService paymentMessageService) {
        this.planService = planService;
        this.subscriptionService = subscriptionService;
        this.localisationService = localisationService;
        this.webMoneyProperties = webMoneyProperties;
        this.userService = userService;
        this.paymentMessageService = paymentMessageService;
    }

    public PaymentProcessResult processPayment(WebMoneyPayment webMoneyPayment) {
        Plan plan = planService.getPlan(webMoneyPayment.planId());
        validate(plan);
        validate(plan, webMoneyPayment);

        LocalDate localDate = subscriptionService.renewSubscription(plan, webMoneyPayment.userId());
        long chatId = userService.getChatId(webMoneyPayment.userId());
        Integer paymentMessageId = null;
        try {
            paymentMessageId = paymentMessageService.delete(chatId);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return new PaymentProcessResult(localDate, chatId, paymentMessageId);
    }

    public Map<String, Object> processPaymentRequest(int planId, int userId, PaymentType paymentType) {
        Plan plan = planService.getPlan(planId);
        validate(plan);

        return Map.of(
                "planId", planId,
                "userId", userId,
                "paymentType", paymentType.getType(),
                "price", plan.getPrice(),
                "paymentDescription", Base64.getEncoder().encodeToString(plan.getPaymentDescription().getBytes(StandardCharsets.UTF_8)),
                "payeePurse", webMoneyProperties.getPurse());
    }

    private void validate(Plan plan) {
        if (!plan.isActive()) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_INVALID_PLAN));
        }
    }

    private void validate(Plan plan, WebMoneyPayment webMoneyPayment) {
        if (!webMoneyProperties.getSecretKey().equals(webMoneyPayment.secretKey())) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_PAYMENT_REQUEST));
        }
        validate(plan, webMoneyPayment.paymentAmount());
        if (!webMoneyProperties.getPurse().equals(webMoneyPayment.payeePurse())) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_PAYMENT_REQUEST));
        }
    }

    private void validate(Plan plan, double paymentAmount) {
        if (plan.getPrice() > paymentAmount) {
            throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BAD_PAYMENT_REQUEST));
        }
    }

    public static class PaymentProcessResult {

        private LocalDate subscriptionEnd;

        private long chatId;

        private Integer paymentMessageId;

        private PaymentProcessResult(LocalDate subscriptionEnd, long chatId, Integer paymentMessageId) {
            this.subscriptionEnd = subscriptionEnd;
            this.chatId = chatId;
            this.paymentMessageId = paymentMessageId;
        }

        public LocalDate getSubscriptionEnd() {
            return subscriptionEnd;
        }

        public long getChatId() {
            return chatId;
        }

        public Integer getPaymentMessageId() {
            return paymentMessageId;
        }
    }
}
