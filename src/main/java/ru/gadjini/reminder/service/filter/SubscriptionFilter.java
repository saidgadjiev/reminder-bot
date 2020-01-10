package ru.gadjini.reminder.service.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.model.TgMessage;
import ru.gadjini.reminder.service.command.CommandParser;
import ru.gadjini.reminder.service.keyboard.InlineKeyboardService;
import ru.gadjini.reminder.service.keyboard.ReplyKeyboardService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;

import java.time.LocalDate;

@Component
public class SubscriptionFilter extends BaseBotFilter {

    private CommandParser commandParser;

    private MessageService messageService;

    private LocalisationService localisationService;

    private SubscriptionService subscriptionService;

    private PlanService planService;

    private ReplyKeyboardService replyKeyboardService;

    private InlineKeyboardService inlineKeyboardService;

    @Autowired
    public SubscriptionFilter(CommandParser commandParser, MessageService messageService,
                              LocalisationService localisationService, SubscriptionService subscriptionService,
                              PlanService planService, ReplyKeyboardService replyKeyboardService, InlineKeyboardService inlineKeyboardService) {
        this.commandParser = commandParser;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.subscriptionService = subscriptionService;
        this.planService = planService;
        this.replyKeyboardService = replyKeyboardService;
        this.inlineKeyboardService = inlineKeyboardService;
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
            sendSubscriptionExpired(userId, chatId);

            return false;
        }

        return true;
    }

    private void sendSubscriptionExpired(int userId, long chatId) {
        messageService.sendMessage(chatId, localisationService.getMessage(MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED), replyKeyboardService.removeKeyboard());

        Plan plan = planService.getActivePlan();
        messageService.sendMessage(chatId, getNeedPayMessage(plan.getDescription()), inlineKeyboardService.getPaymentKeyboard(userId, plan.getId()));
    }

    private String getNeedPayMessage(String planDesc) {
        return planDesc + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_PAYMENT_TYPE);
    }
}
