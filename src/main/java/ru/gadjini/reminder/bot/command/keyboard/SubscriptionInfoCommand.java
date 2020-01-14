package ru.gadjini.reminder.bot.command.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.time.TimeBuilder;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;
import ru.gadjini.reminder.time.DateTimeFormats;

@Component
public class SubscriptionInfoCommand implements KeyboardBotCommand {

    private String name;

    private final LocalisationService localisationService;

    private SubscriptionService subscriptionService;

    private MessageService messageService;

    private PlanService planService;

    private TimeBuilder timeBuilder;

    public SubscriptionInfoCommand(LocalisationService localisationService,
                                   SubscriptionService subscriptionService,
                                   MessageService messageService, PlanService planService, TimeBuilder timeBuilder) {
        this.name = localisationService.getMessage(MessagesProperties.SUBSCRIPTION_COMMAND_NAME);
        this.localisationService = localisationService;
        this.subscriptionService = subscriptionService;
        this.messageService = messageService;
        this.planService = planService;
        this.timeBuilder = timeBuilder;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return name.equals(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        Subscription subscription = subscriptionService.getSubscription(message.getFrom().getId());

        messageService.sendMessage(new SendMessageContext().chatId(message.getChatId()).text(getSubscriptionInfo(subscription)));

        return false;
    }

    private String getSubscriptionInfo(Subscription subscription) {
        Plan plan = planService.getActivePlan();

        if (subscription.getPlanId() == null) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_END_DATE,
                    new Object[]{DateTimeFormats.PAYMENT_PERIOD_PATTERN.format(subscription.getEndDate()), plan.getPrice(), timeBuilder.time(plan.getPeriod())}
            );
        }

        return localisationService.getMessage(
                MessagesProperties.MESSAGE_SUBSCRIPTION_END_DATE,
                new Object[]{DateTimeFormats.PAYMENT_PERIOD_PATTERN.format(subscription.getEndDate()), plan.getPrice(), timeBuilder.time(plan.getPeriod())}
        );
    }
}
