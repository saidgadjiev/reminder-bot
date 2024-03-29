package ru.gadjini.reminder.bot.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.reminder.bot.command.api.KeyboardBotCommand;
import ru.gadjini.reminder.common.MessagesProperties;
import ru.gadjini.reminder.domain.Plan;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.job.PriorityJob;
import ru.gadjini.reminder.model.SendMessageContext;
import ru.gadjini.reminder.service.TgUserService;
import ru.gadjini.reminder.service.message.LocalisationService;
import ru.gadjini.reminder.service.message.MessageService;
import ru.gadjini.reminder.service.reminder.time.Time2TextService;
import ru.gadjini.reminder.service.subscription.PlanService;
import ru.gadjini.reminder.service.subscription.SubscriptionService;
import ru.gadjini.reminder.time.DateTimeFormats;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

//@Component
public class SubscriptionInfoCommand implements KeyboardBotCommand {

    private Set<String> names = new HashSet<>();

    private final LocalisationService localisationService;

    private SubscriptionService subscriptionService;

    private MessageService messageService;

    private PlanService planService;

    private Time2TextService timeBuilder;

    private TgUserService userService;

    public SubscriptionInfoCommand(LocalisationService localisationService,
                                   SubscriptionService subscriptionService,
                                   MessageService messageService, PlanService planService, Time2TextService timeBuilder, TgUserService userService) {
        this.localisationService = localisationService;
        this.subscriptionService = subscriptionService;
        this.messageService = messageService;
        this.planService = planService;
        this.timeBuilder = timeBuilder;
        this.userService = userService;

        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.SUBSCRIPTION_COMMAND_NAME, locale));
        }
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        Subscription subscription = subscriptionService.getSubscription(message.getFrom().getId());

        Locale locale = userService.getLocale(message.getFrom().getId());
        messageService.sendMessageAsync(
                new SendMessageContext(PriorityJob.Priority.MEDIUM)
                        .chatId(message.getChatId())
                        .text(getSubscriptionInfo(subscription, locale))
        );

        return false;
    }

    private String getSubscriptionInfo(Subscription subscription, Locale locale) {
        Plan plan = planService.getActivePlan();

        if (subscription.getPlanId() == null) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_END_DATE,
                    new Object[]{DateTimeFormats.PAYMENT_PERIOD_PATTERN.format(subscription.getEndDate()), plan.getPrice(), timeBuilder.time(plan.getPeriod(), locale)},
                    locale);
        }

        return localisationService.getMessage(
                MessagesProperties.MESSAGE_SUBSCRIPTION_END_DATE,
                new Object[]{DateTimeFormats.PAYMENT_PERIOD_PATTERN.format(subscription.getEndDate()), plan.getPrice(), timeBuilder.time(plan.getPeriod(), locale)},
                locale);
    }
}
