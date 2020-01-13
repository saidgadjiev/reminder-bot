package ru.gadjini.reminder.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.reply.CurrReplyKeyboard;
import ru.gadjini.reminder.service.keyboard.reply.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.subscription.PaymentMessageService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;

import java.time.LocalDate;

@Component
public class SubscriptionFilter extends BaseBotFilter {

    private MessageService messageService;

    private LocalisationService localisationService;

    private SubscriptionService subscriptionService;

    private PlanService planService;

    private ReplyKeyboardService replyKeyboardService;

    private InlineKeyboardService inlineKeyboardService;

    private PaymentMessageService paymentMessageService;

    @Autowired
    public SubscriptionFilter(MessageService messageService,
                              LocalisationService localisationService, SubscriptionService subscriptionService,
                              PlanService planService, CurrReplyKeyboard replyKeyboardService,
                              InlineKeyboardService inlineKeyboardService, PaymentMessageService paymentMessageService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.subscriptionService = subscriptionService;
        this.planService = planService;
        this.replyKeyboardService = replyKeyboardService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.paymentMessageService = paymentMessageService;
    }

    @Override
    public void doFilter(Update update) {
        TgMessage tgMessage = TgMessage.from(update);
        boolean checkSubscriptionResult = checkSubscription(tgMessage.getChatId(), tgMessage.getUser().getId());

        if (checkSubscriptionResult) {
            super.doFilter(update);
        }
    }

    private boolean checkSubscription(long chatId, int userId) {
        Subscription subscription = subscriptionService.getSubscription(userId);

        if (subscription == null) {
            subscriptionService.createTrialSubscription(userId);

            return true;
        }
        if (subscription.getEndDate().isBefore(LocalDate.now())) {
            sendSubscriptionExpired(chatId, userId);

            return false;
        }

        return true;
    }

    private void sendSubscriptionExpired(long chatId, int userId) {
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(chatId)
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED))
                        .replyKeyboard(replyKeyboardService.removeKeyboard(chatId))
        );

        Integer messageId = paymentMessageService.getMessageId(chatId);
        if (messageId != null) {
            messageService.deleteMessage(chatId, messageId);
        }

        Plan plan = planService.getActivePlan();
        messageService.sendMessage(
                new SendMessageContext()
                        .chatId(chatId)
                        .text(getNeedPayMessage(plan.getDescription()))
                        .replyKeyboard(inlineKeyboardService.getPaymentKeyboard(userId, plan.getId())),
                message -> paymentMessageService.create(chatId, message.getMessageId())
        );
    }

    private String getNeedPayMessage(String planDesc) {
        return planDesc + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PAYMENT_TYPE);
    }
}
